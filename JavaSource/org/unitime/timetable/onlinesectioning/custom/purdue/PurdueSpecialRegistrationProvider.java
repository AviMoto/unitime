/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.cpsolver.coursett.model.Placement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.interfaces.ExternalClassLookupInterface;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.SpecialRegistrationProvider;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Change;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeError;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeOperation;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.EligibilityProblem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Problem;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RequestStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ResponseStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Schedule;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationResponseList;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistrationStatusResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationCheckRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationCheckResponse;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.util.DefaultExternalClassLookup;
import org.unitime.timetable.util.NameFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
public class PurdueSpecialRegistrationProvider implements SpecialRegistrationProvider {
	private static Logger sLog = Logger.getLogger(PurdueSpecialRegistrationProvider.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private ExternalClassLookupInterface iExternalClassLookup;
	
	public PurdueSpecialRegistrationProvider() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
		try {
			String clazz = ApplicationProperty.CustomizationExternalClassLookup.value();
			if (clazz == null || clazz.isEmpty())
				iExternalClassLookup = new DefaultExternalClassLookup();
			else
				iExternalClassLookup = (ExternalClassLookupInterface)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external class lookup, using the default one instead.", e);
			iExternalClassLookup = new DefaultExternalClassLookup();
		}
	}
	
	protected String getSpecialRegistrationApiSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site");
	}
	
	protected String getSpecialRegistrationApiSiteRetrieveRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.retrieveRegistration", getSpecialRegistrationApiSite() + "/retrieveRegistration");
	}
	
	protected String getSpecialRegistrationApiSiteSubmitRegistration() {
		return ApplicationProperties.getProperty("purdue.specreg.site.submitRegistration", getSpecialRegistrationApiSite() + "/submitRegistration");
	}

	protected String getSpecialRegistrationApiSiteCheckEligibility() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkEligibility", getSpecialRegistrationApiSite() + "/checkEligibility");
	}
	
	protected String getSpecialRegistrationApiSiteGetAllRegistrations() {
		return ApplicationProperties.getProperty("purdue.specreg.site.retrieveAllRegistrations", null); //getSpecialRegistrationApiSite() + "/retrieveAllRegistrations");
	}
	
	protected String getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus() {
		return ApplicationProperties.getProperty("purdue.specreg.site.checkSpecialRegistrationStatus", getSpecialRegistrationApiSite() + "/checkSpecialRegistrationStatus");
	}
	
	protected String getSpecialRegistrationApiValidationSite() {
		return ApplicationProperties.getProperty("purdue.specreg.site.validation", getSpecialRegistrationApiSite() + "/checkRestrictionsForSTAR");
	}
	
	protected String getSpecialRegistrationApiKey() {
		return ApplicationProperties.getProperty("purdue.specreg.apiKey");
	}
	
	protected String getSpecialRegistrationMode() {
		return ApplicationProperties.getProperty("purdue.specreg.mode", "REG");
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getBannerCampus(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalCampus(session);
	}
	
	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getRequestorId(OnlineSectioningLog.Entity user) {
		if (user == null || user.getExternalId() == null) return null;
		String id = user.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getRequestorType(OnlineSectioningLog.Entity user, XStudent student) {
		if (user == null || user.getExternalId() == null) return null;
		if (user.hasType()) return user.getType().name();
		return (user.getExternalId().equals(student.getExternalId()) ? "STUDENT" : "MANAGER");
	}
	
	protected SpecialRegistrationStatus getStatus(String status) {
		if (RequestStatus.mayEdit.name().equals(status) || RequestStatus.newRequest.name().equals(status) || RequestStatus.draft.name().equals(status))
			return SpecialRegistrationStatus.Draft;
		else if (RequestStatus.approved.name().equals(status))
			return SpecialRegistrationStatus.Approved;
		else if (RequestStatus.cancelled.name().equals(status))
			return SpecialRegistrationStatus.Cancelled;
		else if (RequestStatus.denied.name().equals(status))
			return SpecialRegistrationStatus.Rejected;
		else
			return SpecialRegistrationStatus.Pending;
	}
	
	protected void buildChangeList(SpecialRegistrationRequest request, OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Collection<ClassAssignmentInterface.ClassAssignment> assignment, Collection<ErrorMessage> errors) {
		request.changes = new ArrayList<Change>();
		request.maxCredit = 0f;
		Map<XCourse, List<XSection>> enrollments = new HashMap<XCourse, List<XSection>>();
		Map<Long, XOffering> offerings = new HashMap<Long, XOffering>();
		for (ClassAssignmentInterface.ClassAssignment ca: assignment) {
			// Skip free times and dummy sections
			if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
			
			XCourse course = server.getCourse(ca.getCourseId());
			if (course == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			XOffering offering = server.getOffering(course.getOfferingId());
			if (offering == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(MSG.courseName(ca.getSubject(), ca.getClassNumber())));
			
			// Check section limits
			XSection section = offering.getSection(ca.getClassId());
			if (section == null)
				throw new SectioningException(MSG.exceptionEnrollNotAvailable(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
			
			// Check cancelled flag
			if (section.isCancelled()) {
				if (server.getConfig().getPropertyBoolean("Enrollment.CanKeepCancelledClass", false)) {
					boolean contains = false;
					for (XRequest r: student.getRequests())
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							if (cr.getEnrollment() != null && cr.getEnrollment().getSectionIds().contains(section.getSectionId())) { contains = true; break; }
						}
					if (!contains)
						throw new SectioningException(MSG.exceptionEnrollCancelled(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				} else {
					throw new SectioningException(MSG.exceptionEnrollCancelled(MSG.clazz(ca.getSubject(), ca.getCourseNbr(), ca.getSubpart(), ca.getSection())));
				}
			}
			
			List<XSection> sections = enrollments.get(course);
			if (sections == null) {
				sections = new ArrayList<XSection>();
				enrollments.put(course, sections);
			}
			sections.add(section);
			offerings.put(course.getCourseId(), offering);
		}
		Set<String> crns = new HashSet<String>();
		check: for (Map.Entry<XCourse, List<XSection>> e: enrollments.entrySet()) {
			XCourse course = e.getKey();
			List<XSection> sections = e.getValue();
			
			if (course.hasCredit())
				request.maxCredit += course.getMinCredit();
			
			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
					if (enrollment != null && enrollment.getCourseId().equals(course.getCourseId())) { // course change
						for (XSection s: sections) {
							if (!enrollment.getSectionIds().contains(s.getSectionId())) {
								Change ch = new Change();
								ch.subject = course.getSubjectArea();
								ch.courseNbr = course.getCourseNumber();
								ch.crn = s.getExternalId(course.getCourseId());
								ch.operation = ChangeOperation.ADD.name();
								if (crns.add(ch.crn)) request.changes.add(ch);
							}
						}
						for (Long id: enrollment.getSectionIds()) {
							XSection s = offerings.get(course.getCourseId()).getSection(id);
							if (!sections.contains(s)) {
								Change ch = new Change();
								ch.subject = course.getSubjectArea();
								ch.courseNbr = course.getCourseNumber();
								ch.crn = s.getExternalId(course.getCourseId());
								ch.operation = ChangeOperation.DROP.name();
								if (crns.add(ch.crn)) request.changes.add(ch);
							}
						}
						continue check;
					}
				}
			}
			
			// new course
			for (XSection section: sections) {
				Change ch = new Change();
				ch.subject = course.getSubjectArea();
				ch.courseNbr = course.getCourseNumber();
				ch.crn = section.getExternalId(course.getCourseId());
				ch.operation = ChangeOperation.ADD.name();
				if (crns.add(ch.crn)) request.changes.add(ch);
			}
		}
		
		// drop course
		for (XRequest r: student.getRequests()) {
			if (r instanceof XCourseRequest) {
				XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
				if (enrollment != null && !offerings.containsKey(enrollment.getCourseId())) {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					if (offering != null)
						for (XSection section: offering.getSections(enrollment)) {
							XCourse course = offering.getCourse(enrollment.getCourseId());
							Change ch = new Change();
							ch.subject = course.getSubjectArea();
							ch.courseNbr = course.getCourseNumber();
							ch.crn = section.getExternalId(course.getCourseId());
							ch.operation = ChangeOperation.DROP.name();
							if (crns.add(ch.crn)) request.changes.add(ch);
						}
				}
			}
		}
		
		if (errors != null) {
			Set<ErrorMessage> added = new HashSet<ErrorMessage>();
			for (Change ch: request.changes) {
				for (ErrorMessage m: errors)
					if (ch.crn.equals(m.getSection()) && added.add(m)) {
						if (ch.errors == null) ch.errors = new ArrayList<ChangeError>();
						ChangeError er = new ChangeError();
						er.code = m.getCode();
						er.message = m.getMessage();
						ch.errors.add(er);
					}
			}
		}
	}

	@Override
	public SpecialRegistrationEligibilityResponse checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistrationEligibilityRequest input) throws SectioningException {
		if (student == null) return new SpecialRegistrationEligibilityResponse(false, "No student.");
		if (!isSpecialRegistrationEnabled(server, helper, student)) return new SpecialRegistrationEligibilityResponse(false, "Special registration is disabled.");
		ClientResource resource = null;
		try {
			if (getSpecialRegistrationApiSiteCheckEligibility() != null) {
				/** -- POST
				Gson gson = getGson(helper);
				SpecialRegistrationRequest request = new SpecialRegistrationRequest();
				AcademicSessionInfo session = server.getAcademicSession();
				request.term = getBannerTerm(session);
				request.campus = getBannerCampus(session);
				request.studentId = getBannerId(student);
				request.changes = buildChangeList(server, helper, student, input.getClassAssignments(), input.getErrors());
				
				if (request.changes == null || request.changes.isEmpty())
					return new SpecialRegistrationEligibilityResponse(false, "There are no changes.");

				resource = new ClientResource(getSpecialRegistrationApiSiteCheckEligibility());
				resource.setNext(iClient);
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				if (helper.isDebugEnabled())
					helper.debug("Request: " + gson.toJson(request));
				helper.getAction().addOptionBuilder().setKey("specreg_request").setValue(gson.toJson(request));
				
				long t1 = System.currentTimeMillis();
				
				resource.post(new GsonRepresentation<SpecialRegistrationRequest>(request));
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationResponse response = (SpecialRegistrationResponse)new GsonRepresentation<SpecialRegistrationResponse>(resource.getResponseEntity(), SpecialRegistrationResponse.class).getObject();
				
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(response));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
				
				return new SpecialRegistrationEligibilityResponse(response != null && ResponseStatus.success.name().equals(response.status), response != null ? response.message : null);
				*/
				resource = new ClientResource(getSpecialRegistrationApiSiteCheckEligibility());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				resource.addQueryParameter("mode", getSpecialRegistrationMode());
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t0 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
				
				SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse eligibility = (SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse)
						new GsonRepresentation<SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse>(resource.getResponseEntity(), SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse.class).getObject();
				Gson gson = getGson(helper);
				
				if (helper.isDebugEnabled())
					helper.debug("Eligibility: " + gson.toJson(eligibility));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(eligibility));
				
				if (!ResponseStatus.success.name().equals(eligibility.status))
					return new SpecialRegistrationEligibilityResponse(false, eligibility.message == null || eligibility.message.isEmpty() ? "Failed to check student eligibility (" + eligibility.status + ")." : eligibility.message);
				
				boolean eligible = true;
				if (eligibility.data == null || eligibility.data.eligible == null || !eligibility.data.eligible.booleanValue()) {
					eligible = false;
				}
				String message = null;
				if (eligibility.data != null && eligibility.data.eligibilityProblems != null) {
					for (EligibilityProblem p: eligibility.data.eligibilityProblems)
						if (message == null)
							message = p.message;
						else
							message += "\n" + p.message;
				}
				SpecialRegistrationEligibilityResponse ret = new SpecialRegistrationEligibilityResponse(eligible, message);
				if (ret.isCanSubmit())
					ret.setErrors(validate(server, helper, student, input.getClassAssignments()));
				return ret;
			} else {
				if (!input.hasErrors()) return new SpecialRegistrationEligibilityResponse(true, null);

				Set<String> errors = new HashSet<String>();
				for (ErrorMessage m: input.getErrors())
					if (m.getCode() != null) errors.add(m.getCode());
				if (errors.isEmpty()) return new SpecialRegistrationEligibilityResponse(true, null);
				
				Gson gson = getGson(helper);

				resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("errors").setValue(errors.toString());
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t1 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationStatusResponse response = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
				
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(response));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
				
				if (response != null && ResponseStatus.success.name().equals(response.status) && response.data != null && response.data.overrides != null) {
					for (String error: errors)
						if (!response.data.overrides.contains(error))
							return new SpecialRegistrationEligibilityResponse(false, "Missing " + error + " override.");
					
					return new SpecialRegistrationEligibilityResponse(true, null);
				} else {
					return new SpecialRegistrationEligibilityResponse(false, response != null ? response.message : null);
				}
			}
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected Set<ErrorMessage> validate(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Collection<ClassAssignment> classAssignments) {
		ValidationCheckRequest req = new ValidationCheckRequest();
		req.studentId = getBannerId(student);
		req.term = getBannerTerm(server.getAcademicSession());
		req.campus = getBannerCampus(server.getAcademicSession());
		req.schedule = new ArrayList<Schedule>();
		req.alternatives = new ArrayList<Schedule>();
		req.mode = getSpecialRegistrationMode();
		req.includeReg = "N";
		req.schedule = new ArrayList<Schedule>();
		Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
		Map<String, String> crn2course = new HashMap<String, String>();
		Map<Long, Schedule> schedules = new HashMap<Long, Schedule>();
		List<String> newCourses = new ArrayList<String>();
		if (classAssignments != null)
			for (ClassAssignment ca: classAssignments) {
				if (ca == null || ca.isFreeTime() || ca.getClassId() == null || ca.isDummy() || ca.isTeachingAssignment()) continue;
				XCourse course = server.getCourse(ca.getCourseId());
				if (course == null) continue;
				XOffering offering = server.getOffering(course.getOfferingId());
				if (offering == null) continue;
				XSection section = offering.getSection(ca.getClassId());
				if (section == null) continue;
				Schedule ch = schedules.get(course.getCourseId());
				String crn = section.getExternalId(course.getCourseId());
				if (ch == null) {
					ch = new Schedule();
					schedules.put(course.getCourseId(), ch);
					ch.subject = course.getSubjectArea();
					ch.courseNbr = course.getCourseNumber();
					ch.crns = new HashSet<String>();
					req.schedule.add(ch);
					if (!ca.isSaved())
						newCourses.add(crn);
				}
				ch.crns.add(crn);
				crn2course.put(crn, course.getCourseName());
			}
		if (!req.schedule.isEmpty()) {
			ValidationCheckResponse resp = null;
			ClientResource resource = null;
			try {
				resource = new ClientResource(getSpecialRegistrationApiValidationSite());
				resource.setNext(iClient);
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				Gson gson = getGson(helper);
				if (helper.isDebugEnabled())
					helper.debug("Request: " + gson.toJson(req));
				helper.getAction().addOptionBuilder().setKey("validation_request").setValue(gson.toJson(req));
				long t1 = System.currentTimeMillis();
				
				resource.post(new GsonRepresentation<ValidationCheckRequest>(req));
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				resp = (ValidationCheckResponse)new GsonRepresentation<ValidationCheckResponse>(resource.getResponseEntity(), ValidationCheckResponse.class).getObject();
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(resp));
				helper.getAction().addOptionBuilder().setKey("validation_response").setValue(gson.toJson(resp));
			} catch (SectioningException e) {
				helper.getAction().setApiException(e.getMessage());
				throw (SectioningException)e;
			} catch (Exception e) {
				helper.getAction().setApiException(e.getMessage());
				sLog.error(e.getMessage(), e);
				throw new SectioningException(e.getMessage());
			} finally {
				if (resource != null) {
					if (resource.getResponse() != null) resource.getResponse().release();
					resource.release();
				}
			}
			
			if (resp != null && resp.scheduleRestrictions != null && resp.scheduleRestrictions.problems != null)
				for (Problem problem: resp.scheduleRestrictions.problems) {
					if ("MAXI".equals(problem.code) && !newCourses.isEmpty()) {
						// Move max credit error message to the last added course
						String crn = newCourses.remove(newCourses.size() - 1);
						errors.add(new ErrorMessage(crn2course.get(crn), crn, problem.code, problem.message));
					} else {
						errors.add(new ErrorMessage(crn2course.get(problem.crn), problem.crn, problem.code, problem.message));
					}
				}
		}
		
		return errors;		
	}

	@Override
	public SubmitSpecialRegistrationResponse submitRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SubmitSpecialRegistrationRequest input) throws SectioningException {
		ClientResource resource = null;
		try {
			SpecialRegistrationRequest request = new SpecialRegistrationRequest();
			AcademicSessionInfo session = server.getAcademicSession();
			request.term = getBannerTerm(session);
			request.campus = getBannerCampus(session);
			request.studentId = getBannerId(student);
			buildChangeList(request, server, helper, student, input.getClassAssignments(), input.hasErrors() ? input.getErrors() : validate(server, helper, student, input.getClassAssignments()));
			// buildChangeList(request, server, helper, student, input.getClassAssignments(), validate(server, helper, student, input.getClassAssignments()));
			request.requestId = input.getRequestId();
			request.mode = getSpecialRegistrationMode(); 
			if (helper.getUser() != null) {
				request.requestorId = getRequestorId(helper.getUser());
				request.requestorRole = getRequestorType(helper.getUser(), student);
			}
			
			if (request.changes == null || request.changes.isEmpty())
				throw new SectioningException("There are no changes.");

			resource = new ClientResource(getSpecialRegistrationApiSiteSubmitRegistration());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			if (input.getRequestKey() != null && !input.getRequestKey().isEmpty())
				resource.addQueryParameter("reqKey", input.getRequestKey());
			
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Request: " + gson.toJson(request));
			helper.getAction().addOptionBuilder().setKey("specreg_request").setValue(gson.toJson(request));
			long t1 = System.currentTimeMillis();
			
			resource.post(new GsonRepresentation<SpecialRegistrationRequest>(request));
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationResponse response = (SpecialRegistrationResponse)new GsonRepresentation<SpecialRegistrationResponse>(resource.getResponseEntity(), SpecialRegistrationResponse.class).getObject();
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			SubmitSpecialRegistrationResponse ret = new SubmitSpecialRegistrationResponse();
			ret.setMessage(response.message);
			ret.setSuccess(ResponseStatus.success.name().equals(response.status));
			if (response.data != null) {
				ret.setStatus(getStatus(response.data.status));
			} else {
				ret.setSuccess(false);
			}
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}

	@Override
	public void dispose() {
		try {
			iClient.stop();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}	
	}
	
	protected CourseOffering findCourseByExternalId(Long sessionId, String externalId) {
		return iExternalClassLookup.findCourseByExternalId(sessionId, externalId);
	}
	
	protected List<Class_> findClassesByExternalId(Long sessionId, String externalId) {
		return iExternalClassLookup.findClassesByExternalId(sessionId, externalId);
	}
	
	protected boolean isDrop(XEnrollment enrollment,  List<Change> changes) {
		return false;
	}
	
	protected List<XRequest> getRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, Map<CourseOffering, List<Class_>> adds, Map<CourseOffering, List<Class_>> drops) {
		Student dbStudent = StudentDAO.getInstance().get(student.getStudentId(), helper.getHibSession());
		List<XRequest> requests = new ArrayList<XRequest>();
		Set<CourseOffering> remaining = new HashSet<CourseOffering>(adds.keySet());
		
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				List<Class_> add = null;
				List<Class_> drop = null;
				XCourseId courseId = null;
				Long configId = null;
				for (XCourseId course: ((XCourseRequest)request).getCourseIds()) {
					for (Map.Entry<CourseOffering, List<Class_>> e: adds.entrySet()) 
						if (course.getCourseId().equals(e.getKey().getUniqueId())) {
							add = e.getValue();
							courseId = course;
							configId = e.getValue().iterator().next().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
							remaining.remove(e.getKey());
						}
					for (Map.Entry<CourseOffering, List<Class_>> e: drops.entrySet()) 
						if (course.getCourseId().equals(e.getKey().getUniqueId())) {
							drop = e.getValue();
						}
				}
				if (add == null && drop == null) {
					// no change detected
					requests.add(request);
				} else {
					XEnrollment enrollemnt = cr.getEnrollment();
					Set<Long> classIds = (enrollemnt == null ? new HashSet<Long>() : new HashSet<Long>(enrollemnt.getSectionIds()));
					if (enrollemnt != null) {
						if (courseId != null) { // add -> check course & config
							if (!enrollemnt.getCourseId().equals(courseId.getCourseId()) && drop == null) {
								// different course and no drop -> create new course request
								requests.add(request);
								remaining.add(CourseOfferingDAO.getInstance().get(courseId.getCourseId(), helper.getHibSession()));
								continue;
							} else if (!enrollemnt.getConfigId().equals(configId)) {
								// same course different config -> drop all
								classIds.clear();
							}
						} else {
							courseId = enrollemnt;
							configId = enrollemnt.getConfigId();
						}
					}
					if (add != null)
						for (Class_ c: add) classIds.add(c.getUniqueId());
					if (drop != null)
						for (Class_ c: drop) classIds.remove(c.getUniqueId());
					if (classIds.isEmpty()) {
						requests.add(new XCourseRequest(cr, null));
					} else {
						requests.add(new XCourseRequest(cr, new XEnrollment(dbStudent, courseId, configId, classIds)));
					}
				}
			} else {
				// free time --> no change
				requests.add(request);
			}
		}
		for (CourseOffering course: remaining) {
			Long configId = null;
			Set<Long> classIds = new HashSet<Long>();
			for (Class_ clazz: adds.get(course)) {
				if (configId == null) configId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
				classIds.add(clazz.getUniqueId());
			}
			XCourseId courseId = new XCourseId(course);
			requests.add(new XCourseRequest(dbStudent, courseId, requests.size(), new XEnrollment(dbStudent, courseId, configId, classIds)));
		}
		return requests;
	}
	
	protected void checkRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, List<XRequest> xrequests, Set<ErrorMessage> errors, boolean allowTimeConf, boolean allowSpaceConf) {
		List<EnrollmentRequest> requests = new ArrayList<EnrollmentRequest>();
		Hashtable<Long, XOffering> courseId2offering = new Hashtable<Long, XOffering>();
		for (XRequest req: xrequests) {
			if (!(req instanceof XCourseRequest)) continue;
			XCourseRequest courseReq = (XCourseRequest)req;
			XEnrollment e = courseReq.getEnrollment();
			if (e == null) continue;
			XCourse course = server.getCourse(e.getCourseId());
			if (course == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(e.getCourseName()));
			EnrollmentRequest request = new EnrollmentRequest(course, new ArrayList<XSection>());
			requests.add(request);
			XOffering offering = server.getOffering(course.getOfferingId());
			if (offering == null)
				throw new SectioningException(MSG.exceptionCourseDoesNotExist(e.getCourseName()));
			for (Long sectionId: e.getSectionIds()) {
				// Check section limits
				XSection section = offering.getSection(sectionId);
				if (section == null)
					throw new SectioningException(MSG.exceptionEnrollNotAvailable(e.getCourseName() + " " + sectionId));
				
				// Check cancelled flag
				if (section.isCancelled()) {
					if (server.getConfig().getPropertyBoolean("Enrollment.CanKeepCancelledClass", false)) {
						boolean contains = false;
						for (XRequest r: student.getRequests())
							if (r instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)r;
								if (cr.getEnrollment() != null && cr.getEnrollment().getSectionIds().contains(section.getSectionId())) { contains = true; break; }
							}
						if (!contains)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_CANCEL, MSG.exceptionEnrollCancelled(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
					} else
						errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_CANCEL, MSG.exceptionEnrollCancelled(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
				}
				request.getSections().add(section);
				courseId2offering.put(course.getCourseId(), offering);
			}
		}
			
		// Check for NEW and CHANGE deadlines
		check: for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			List<XSection> sections = request.getSections();

			for (XRequest r: student.getRequests()) {
				if (r instanceof XCourseRequest) {
					XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
					if (enrollment != null && enrollment.getCourseId().equals(course.getCourseId())) { // course change
						for (XSection s: sections)
							if (!enrollment.getSectionIds().contains(s.getSectionId()) && !server.checkDeadline(course.getCourseId(), s.getTime(), OnlineSectioningServer.Deadline.CHANGE))
								errors.add(new ErrorMessage(course.getCourseName(), s.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineChange(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), s.getSubpartName(), s.getName(course.getCourseId())))));
						continue check;
					}
				}
			}
			
			// new course
			for (XSection section: sections) {
				if (!server.checkDeadline(course.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.NEW))
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineNew(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName(course.getCourseId())))));
			}
		}
		
		// Check for DROP deadlines
		for (XRequest r: student.getRequests()) {
			if (r instanceof XCourseRequest) {
				XEnrollment enrollment = ((XCourseRequest)r).getEnrollment();
				if (enrollment != null && !courseId2offering.containsKey(enrollment.getCourseId())) {
					XOffering offering = server.getOffering(enrollment.getOfferingId());
					if (offering != null)
						for (XSection section: offering.getSections(enrollment)) {
							if (!server.checkDeadline(offering.getOfferingId(), section.getTime(), OnlineSectioningServer.Deadline.DROP))
								errors.add(new ErrorMessage(enrollment.getCourseName(), section.getExternalId(enrollment.getCourseId()), ErrorMessage.UniTimeCode.UT_DEADLINE, MSG.exceptionEnrollDeadlineDrop(enrollment.getCourseName())));
						}
				}
			}
		}
		
		Hashtable<Long, XConfig> courseId2config = new Hashtable<Long, XConfig>();
		for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			XOffering offering = courseId2offering.get(course.getCourseId());
			XEnrollments enrollments = server.getEnrollments(course.getOfferingId());
			List<XSection> sections = request.getSections();
			XSubpart subpart = offering.getSubpart(sections.get(0).getSubpartId());
			XConfig config = offering.getConfig(subpart.getConfigId());
			courseId2config.put(course.getCourseId(), config);

			XReservation reservation = null;
			reservations: for (XReservation r: offering.getReservations()) {
				if (!r.isApplicable(student, course)) continue;
				if (r.getLimit() >= 0 && r.getLimit() <= enrollments.countEnrollmentsForReservation(r.getReservationId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForReservation(r.getReservationId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain) continue;
				}
				if (!r.getConfigsIds().isEmpty() && !r.getConfigsIds().contains(config.getConfigId())) continue;
				for (XSection section: sections)
					if (r.getSectionIds(section.getSubpartId()) != null && !r.getSectionIds(section.getSubpartId()).contains(section.getSectionId())) continue reservations;
				if (reservation == null || r.compareTo(reservation) < 0)
					reservation = r;
			}
			
			if ((reservation == null || !reservation.canAssignOverLimit()) && !allowSpaceConf) {
				for (XSection section: sections) {
					if (section.getLimit() >= 0 && section.getLimit() <= enrollments.countEnrollmentsForSection(section.getSectionId())) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
					}
					if ((reservation == null || !offering.getSectionReservations(section.getSectionId()).contains(reservation)) && offering.getUnreservedSectionSpace(section.getSectionId(), enrollments) <= 0) {
						boolean contain = false;
						for (XEnrollment e: enrollments.getEnrollmentsForSection(section.getSectionId()))
							if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
						if (!contain)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
					}
				}
				
				if (config.getLimit() >= 0 && config.getLimit() <= enrollments.countEnrollmentsForConfig(config.getConfigId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
				if ((reservation == null || !offering.getConfigReservations(config.getConfigId()).contains(reservation)) && offering.getUnreservedConfigSpace(config.getConfigId(), enrollments) <= 0) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForConfig(config.getConfigId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
				
				if (course.getLimit() >= 0 && course.getLimit() <= enrollments.countEnrollmentsForCourse(course.getCourseId())) {
					boolean contain = false;
					for (XEnrollment e: enrollments.getEnrollmentsForCourse(course.getCourseId()))
						if (e.getStudentId().equals(student.getStudentId())) { contain = true; break; }
					if (!contain)
						for (XSection section: sections)
							errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_NOT_AVAILABLE, MSG.exceptionEnrollNotAvailable(MSG.clazz(course.getSubjectArea(), course.getCourseNumber(), section.getSubpartName(), section.getName()))));
				}
			}
		}
		
		for (EnrollmentRequest request: requests) {
			XCourse course = request.getCourse();
			XOffering offering = courseId2offering.get(course.getCourseId());
			List<XSection> sections = request.getSections();
			XSubpart subpart = offering.getSubpart(sections.get(0).getSubpartId());
			XConfig config = offering.getConfig(subpart.getConfigId());
			if (sections.size() < config.getSubparts().size()) {
				for (XSection section: sections)
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentIncomplete(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			} else if (sections.size() > config.getSubparts().size()) {
				for (XSection section: sections)
					errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			}
			for (XSection s1: sections) {
				for (XSection s2: sections) {
					if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(offering.getDistributions(), s2)) {
						errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF, MSG.exceptionEnrollmentOverlapping(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
						errors.add(new ErrorMessage(course.getCourseName(), s2.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF, MSG.exceptionEnrollmentOverlapping(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
					if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) {
						errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
						errors.add(new ErrorMessage(course.getCourseName(), s2.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
				}
				if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId()))
					errors.add(new ErrorMessage(course.getCourseName(), s1.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_STRUCTURE, MSG.exceptionEnrollmentInvalid(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
			}
			if (!offering.isAllowOverlap(student, config.getConfigId(), course, sections) && !allowTimeConf)
				for (EnrollmentRequest otherRequest: requests) {
					XOffering other = courseId2offering.get(otherRequest.getCourse().getCourseId());
					XConfig otherConfig = courseId2config.get(otherRequest.getCourse().getCourseId());
					if (!other.equals(offering) && !other.isAllowOverlap(student, otherConfig.getConfigId(), otherRequest.getCourse(), otherRequest.getSections())) {
						List<XSection> assignment = otherRequest.getSections();
						for (XSection section: sections)
							if (section.isOverlapping(offering.getDistributions(), assignment))
								errors.add(new ErrorMessage(course.getCourseName(), section.getExternalId(course.getCourseId()), ErrorMessage.UniTimeCode.UT_TIME_CNF,MSG.exceptionEnrollmentConflicting(MSG.courseName(course.getSubjectArea(), course.getCourseNumber()))));
					}
				}
		}
	}
	
	protected RetrieveSpecialRegistrationResponse convert(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, SpecialRegistrationRequest specialRequest) {
		RetrieveSpecialRegistrationResponse ret = new RetrieveSpecialRegistrationResponse();
		Map<CourseOffering, List<Class_>> adds = new HashMap<CourseOffering, List<Class_>>();
		Map<CourseOffering, List<Class_>> drops = new HashMap<CourseOffering, List<Class_>>();
		Set<ErrorMessage> errors = new TreeSet<ErrorMessage>();
		TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
		if (specialRequest.changes != null)
			for (Change change: specialRequest.changes) {
				CourseOffering course = findCourseByExternalId(server.getAcademicSession().getUniqueId(), change.crn);
				List<Class_> classes = findClassesByExternalId(server.getAcademicSession().getUniqueId(), change.crn);
				if (course != null && classes != null && !classes.isEmpty()) {
					courses.add(course);
					List<Class_> list = (!ChangeOperation.DROP.name().equals(change.operation) ? adds : drops).get(course);
					if (list == null) {
						list = new ArrayList<Class_>();
						 (!ChangeOperation.DROP.name().equals(change.operation) ? adds : drops).put(course, list);
					}
					for (Class_ clazz: classes)
						list.add(clazz);
					if (change.errors != null)
						for (ChangeError err: change.errors)
							for (Class_ clazz: classes)
								errors.add(new ErrorMessage(course.getCourseName(), clazz.getExternalId(course), err.code, err.message));
				}
			}
		String desc = "";
		NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
		for (CourseOffering course: courses) {
			if (!desc.isEmpty()) desc += ", ";
			desc += course.getCourseName();
			if (adds.containsKey(course)) {
				if (drops.containsKey(course)) {
					desc += " (change)";
				} else {
					desc += " (add)";
				}
			} else if (drops.containsKey(course)) {
				desc += " (drop)";
			}
			CourseCreditUnitConfig credit = course.getCredit();
			if (adds.containsKey(course)) {
				for (Class_ clazz: adds.get(course)) {
					ClassAssignment ca = new ClassAssignment();
					ca.setCourseId(course.getUniqueId());
					ca.setSubject(course.getSubjectAreaAbbv());
					ca.setCourseNbr(course.getCourseNbr());
					ca.setCourseAssigned(true);
					ca.setTitle(course.getTitle());
					ca.setClassId(clazz.getUniqueId());
					ca.setSection(clazz.getClassSuffix(course));
					if (ca.getSection() == null)
						ca.setSection(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setClassNumber(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
					ca.setExternalId(clazz.getExternalId(course));
					if (clazz.getParentClass() != null) {
						ca.setParentSection(clazz.getParentClass().getClassSuffix(course));
						if (ca.getParentSection() == null)
							ca.setParentSection(clazz.getParentClass().getSectionNumberString(helper.getHibSession()));
					}
					if (clazz.getSchedulePrintNote() != null)
						ca.addNote(clazz.getSchedulePrintNote());
					Placement placement = clazz.getCommittedAssignment() == null ? null : clazz.getCommittedAssignment().getPlacement();
					int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && placement != null) {
                		// int roomLimit = Math.round((enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()) * placement.getRoomSize());
                		int roomLimit = (int) Math.floor(placement.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    ca.setCancelled(clazz.isCancelled());
					ca.setLimit(new int[] { clazz.getEnrollment(), limit});
					if (placement != null) {
						if (placement.getTimeLocation() != null) {
							for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
								ca.addDay(d.getIndex());
							ca.setStart(placement.getTimeLocation().getStartSlot());
							ca.setLength(placement.getTimeLocation().getLength());
							ca.setBreakTime(placement.getTimeLocation().getBreakTime());
							ca.setDatePattern(placement.getTimeLocation().getDatePatternName());
						}
						if (clazz.getCommittedAssignment() != null)
							for (Location loc: clazz.getCommittedAssignment().getRooms())
								ca.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
					}
					if (clazz.getDisplayInstructor())
						for (ClassInstructor ci : clazz.getClassInstructors()) {
							if (!ci.isLead()) continue;
							ca.addInstructor(nameFormat.format(ci.getInstructor()));
							ca.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
						}
					if (clazz.getSchedulingSubpart().getCredit() != null) {
						ca.setCredit(clazz.getSchedulingSubpart().getCredit().creditAbbv() + "|" + clazz.getSchedulingSubpart().getCredit().creditText());
					} else if (credit != null) {
						ca.setCredit(credit.creditAbbv() + "|" + credit.creditText());
					}
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());
					for (ErrorMessage error: errors) {
						if (ca.getCourseName().equals(error.getCourse()) && ca.getExternalId().equals(error.getSection())) {
							if (ca.hasError())
								ca.setError(ca.getError() + "\n" + error.getMessage());
							else
								ca.setError(error.getMessage());
						}
					}
					ret.addChange(ca);
				}
			}
			if (drops.containsKey(course)) {
				for (Class_ clazz: drops.get(course)) {
					ClassAssignment ca = new ClassAssignment();
					ca.setCourseId(course.getUniqueId());
					ca.setSubject(course.getSubjectAreaAbbv());
					ca.setCourseNbr(course.getCourseNbr());
					ca.setCourseAssigned(false);
					ca.setTitle(course.getTitle());
					ca.setClassId(clazz.getUniqueId());
					ca.setSection(clazz.getClassSuffix(course));
					if (ca.getSection() == null)
						ca.setSection(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setClassNumber(clazz.getSectionNumberString(helper.getHibSession()));
					ca.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
					ca.setExternalId(clazz.getExternalId(course));
					if (clazz.getParentClass() != null) {
						ca.setParentSection(clazz.getParentClass().getClassSuffix(course));
						if (ca.getParentSection() == null)
							ca.setParentSection(clazz.getParentClass().getSectionNumberString(helper.getHibSession()));
					}
					if (clazz.getSchedulePrintNote() != null)
						ca.addNote(clazz.getSchedulePrintNote());
					Placement placement = clazz.getCommittedAssignment() == null ? null : clazz.getCommittedAssignment().getPlacement();
					int minLimit = clazz.getExpectedCapacity();
                	int maxLimit = clazz.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && placement != null) {
                		// int roomLimit = Math.round((enrollment.getClazz().getRoomRatio() == null ? 1.0f : enrollment.getClazz().getRoomRatio()) * placement.getRoomSize());
                		int roomLimit = (int) Math.floor(placement.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    ca.setCancelled(clazz.isCancelled());
					ca.setLimit(new int[] { clazz.getEnrollment(), limit});
					if (placement != null) {
						if (placement.getTimeLocation() != null) {
							for (DayCode d : DayCode.toDayCodes(placement.getTimeLocation().getDayCode()))
								ca.addDay(d.getIndex());
							ca.setStart(placement.getTimeLocation().getStartSlot());
							ca.setLength(placement.getTimeLocation().getLength());
							ca.setBreakTime(placement.getTimeLocation().getBreakTime());
							ca.setDatePattern(placement.getTimeLocation().getDatePatternName());
						}
						if (clazz.getCommittedAssignment() != null)
							for (Location loc: clazz.getCommittedAssignment().getRooms())
								ca.addRoom(loc.getUniqueId(), loc.getLabelWithDisplayName());
					}
					if (clazz.getDisplayInstructor())
						for (ClassInstructor ci : clazz.getClassInstructors()) {
							if (!ci.isLead()) continue;
							ca.addInstructor(nameFormat.format(ci.getInstructor()));
							ca.addInstructoEmail(ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
						}
					if (clazz.getSchedulingSubpart().getCredit() != null) {
						ca.setCredit(clazz.getSchedulingSubpart().getCredit().creditAbbv() + "|" + clazz.getSchedulingSubpart().getCredit().creditText());
					} else if (credit != null) {
						ca.setCredit(credit.creditAbbv() + "|" + credit.creditText());
					}
					credit = null;
					if (ca.getParentSection() == null)
						ca.setParentSection(course.getConsentType() == null ? null : course.getConsentType().getLabel());
					for (ErrorMessage error: errors) {
						if (ca.getCourseName().equals(error.getCourse()) && ca.getExternalId().equals(error.getSection())) {
							if (ca.hasError())
								ca.setError(ca.getError() + "\n" + error.getMessage());
							else
								ca.setError(error.getMessage());
						}
					}
					ret.addChange(ca);
				}
			}
		}
		
		List<XRequest> requests = getRequests(server, helper, student, adds, drops);
		checkRequests(server, helper, student, requests, errors, false, false);
		ret.setClassAssignments(GetAssignment.computeAssignment(server, helper, student, requests, null, errors, true));
		if (helper.getAction().getEnrollmentCount() > 0)
			helper.getAction().getEnrollmentBuilder(helper.getAction().getEnrollmentCount() - 1).setType(OnlineSectioningLog.Enrollment.EnrollmentType.EXTERNAL);
		helper.getAction().clearRequest();
		ret.setDescription(desc);
		
		if (ret.hasClassAssignments())
			for (CourseAssignment course: ret.getClassAssignments().getCourseAssignments()) {
				if (course.isFreeTime() || course.isTeachingAssignment()) continue;
				List<Class_> add = null;
				for (Map.Entry<CourseOffering, List<Class_>> e: adds.entrySet())
					if (course.getCourseId().equals(e.getKey().getUniqueId())) { add = e.getValue(); break; }
				if (add != null)
					for (ClassAssignment ca: course.getClassAssignments())
						if (ca.isSaved())
							for (Class_ c: add)
								if (c.getUniqueId().equals(ca.getClassId())) ca.setSaved(false);
			}
		
		ret.setRequestId(specialRequest.requestId);
		ret.setSubmitDate(specialRequest.dateCreated == null ? null : specialRequest.dateCreated.toDate());
		ret.setNote(specialRequest.notes);
		ret.setStatus(getStatus(specialRequest.status));
		return ret;
	}
	
	@Override
	public RetrieveSpecialRegistrationResponse retrieveRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, RetrieveSpecialRegistrationRequest input) throws SectioningException {
		if (student == null) return null;
		ClientResource resource = null;
		try {
			resource = new ClientResource(getSpecialRegistrationApiSiteRetrieveRegistration());
			resource.setNext(iClient);
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			resource.addQueryParameter("reqKey", input.getRequestKey());
			helper.getAction().addOptionBuilder().setKey("reqKey").setValue(input.getRequestKey());

			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationResponse response = (SpecialRegistrationResponse)new GsonRepresentation<SpecialRegistrationResponse>(resource.getResponseEntity(), SpecialRegistrationResponse.class).getObject();
			Gson gson = getGson(helper);
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			if (response.data != null) {
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				if (response.data.campus != null && !campus.equals(response.data.campus))
					throw new SectioningException("Special registration request is for a different campus (" + response.data.campus + ").");
				if (response.data.term != null && !term.equals(response.data.term))
					throw new SectioningException("Special registration request is for a different term (" + response.data.term + ").");
				if (response.data.studentId != null && !getBannerId(student).equals(response.data.studentId))
					throw new SectioningException("Special registration request is for a different student.");
				return convert(server, helper, student, response.data);
			} else if (!ResponseStatus.success.name().equals(response.status)) {
				if (response.message != null && !response.message.isEmpty())
					throw new SectioningException(response.message);
			}
			
			RetrieveSpecialRegistrationResponse ret = new RetrieveSpecialRegistrationResponse();
			ret.setStatus(getStatus(response.status));
			ret.setDescription(response.message != null && !response.message.isEmpty() ? response.message : "New Special Registration");
			// ret.setRequestId(input.getRequestKey());
			return ret;
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected Gson getGson(OnlineSectioningHelper helper) {
		GsonBuilder builder = new GsonBuilder()
		.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
			@Override
			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}
		})
		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsJsonPrimitive().getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e.getMessage(), e);
				}
			}
		});
		if (helper.isDebugEnabled()) builder.setPrettyPrinting();
		return builder.create();
	}

	@Override
	public List<RetrieveSpecialRegistrationResponse> retrieveAllRegistrations(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		if (student == null) return null;
		if (!isSpecialRegistrationEnabled(server, helper, student)) return null;
		ClientResource resource = null;
		try {
			if (getSpecialRegistrationApiSiteGetAllRegistrations() != null) {
				resource = new ClientResource(getSpecialRegistrationApiSiteGetAllRegistrations());
				resource.setNext(iClient);
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t1 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationResponseList specialRequests = (SpecialRegistrationResponseList)new GsonRepresentation<SpecialRegistrationResponseList>(resource.getResponseEntity(), SpecialRegistrationResponseList.class).getObject();
				Gson gson = getGson(helper);
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(specialRequests));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(specialRequests));
				
				if ((specialRequests.data == null || specialRequests.data.isEmpty()) && !ResponseStatus.success.name().equals(specialRequests.status)) {
					throw new SectioningException(specialRequests.message == null || specialRequests.message.isEmpty() ? "Call failed but no message was given." : specialRequests.message);
				}
				
				if (specialRequests.data != null) {
					List<RetrieveSpecialRegistrationResponse> ret = new ArrayList<RetrieveSpecialRegistrationResponse>(specialRequests.data.size());
					for (SpecialRegistrationRequest specialRequest: specialRequests.data)
						if (specialRequest.requestId != null)
							ret.add(convert(server, helper, student, specialRequest));
					return ret;
				}				
			} else {
				resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
				resource.setNext(iClient);
				
				AcademicSessionInfo session = server.getAcademicSession();
				String term = getBannerTerm(session);
				String campus = getBannerCampus(session);
				resource.addQueryParameter("term", term);
				resource.addQueryParameter("campus", campus);
				resource.addQueryParameter("studentId", getBannerId(student));
				helper.getAction().addOptionBuilder().setKey("term").setValue(term);
				helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
				helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
				resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
				
				long t1 = System.currentTimeMillis();
				
				resource.get(MediaType.APPLICATION_JSON);
				
				helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
				
				SpecialRegistrationStatusResponse response = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
				Gson gson = getGson(helper);
				
				if (helper.isDebugEnabled())
					helper.debug("Response: " + gson.toJson(response));
				helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
				
				if (response != null && ResponseStatus.success.name().equals(response.status) && response.data != null && response.data.requests != null) {
					List<RetrieveSpecialRegistrationResponse> ret = new ArrayList<RetrieveSpecialRegistrationResponse>(response.data.requests.size());
					for (SpecialRegistrationRequest specialRequest: response.data.requests)
						if (specialRequest.requestId != null)
							ret.add(convert(server, helper, student, specialRequest));
					return ret;
				}
			}
			
			return new ArrayList<RetrieveSpecialRegistrationResponse>();
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}

	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, XStudent student) throws SectioningException {
		if (student == null || !isSpecialRegistrationEnabled(server, helper, student)) {
			check.setFlag(EligibilityFlag.CAN_SPECREG, false);
			return;
		}
		ClientResource resource = null;
		try {
			Gson gson = getGson(helper);

			resource = new ClientResource(getSpecialRegistrationApiSiteCheckSpecialRegistrationStatus());
			resource.setNext(iClient);
			
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String campus = getBannerCampus(session);
			resource.addQueryParameter("term", term);
			resource.addQueryParameter("campus", campus);
			resource.addQueryParameter("studentId", getBannerId(student));
			helper.getAction().addOptionBuilder().setKey("term").setValue(term);
			helper.getAction().addOptionBuilder().setKey("campus").setValue(campus);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			resource.addQueryParameter("apiKey", getSpecialRegistrationApiKey());
			
			long t1 = System.currentTimeMillis();
			
			resource.get(MediaType.APPLICATION_JSON);
			
			helper.getAction().setApiPostTime(System.currentTimeMillis() - t1);
			
			SpecialRegistrationStatusResponse response = (SpecialRegistrationStatusResponse)new GsonRepresentation<SpecialRegistrationStatusResponse>(resource.getResponseEntity(), SpecialRegistrationStatusResponse.class).getObject();
			
			if (helper.isDebugEnabled())
				helper.debug("Response: " + gson.toJson(response));
			helper.getAction().addOptionBuilder().setKey("specreg_response").setValue(gson.toJson(response));
			
			if (response != null && ResponseStatus.success.name().equals(response.status)) {
				check.setFlag(EligibilityFlag.CAN_SPECREG, true);
				if (response.data != null) 
					check.setOverrides(response.data.overrides);
				check.setFlag(EligibilityFlag.SR_TIME_CONF, check.hasOverride("TIME"));
				check.setFlag(EligibilityFlag.SR_LIMIT_CONF, check.hasOverride("CLOS"));
				check.setFlag(EligibilityFlag.HAS_SPECREG, response.data != null && response.data.requests != null && !response.data.requests.isEmpty());
			} else {
				check.setFlag(EligibilityFlag.CAN_SPECREG, false);
			}
		} catch (SectioningException e) {
			helper.getAction().setApiException(e.getMessage());
			throw (SectioningException)e;
		} catch (Exception e) {
			helper.getAction().setApiException(e.getMessage());
			sLog.error(e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected boolean isSpecialRegistrationEnabled(org.unitime.timetable.model.Student student) {
		if (student == null) return false;
		StudentSectioningStatus status = student.getEffectiveStatus();
		return status == null || status.hasOption(StudentSectioningStatus.Option.specreg);
	}
	
	protected boolean isSpecialRegistrationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		if (student == null) return false;
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.specreg);
	}
}
