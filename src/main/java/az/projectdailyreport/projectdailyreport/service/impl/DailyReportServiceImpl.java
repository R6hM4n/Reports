package az.projectdailyreport.projectdailyreport.service.impl;
import az.projectdailyreport.projectdailyreport.dto.dailyreport.DailyReportAdmin;
import az.projectdailyreport.projectdailyreport.dto.dailyreport.DailyReportDTO;
import az.projectdailyreport.projectdailyreport.dto.dailyreport.DailyReportUpdate;
import az.projectdailyreport.projectdailyreport.dto.dailyreport.DailyReportUser;
import az.projectdailyreport.projectdailyreport.dto.project.ProjectDTO;
import az.projectdailyreport.projectdailyreport.dto.UserDTO;
import az.projectdailyreport.projectdailyreport.dto.request.DailyReportRequest;
import az.projectdailyreport.projectdailyreport.exception.DailyReportUpdateException;
import az.projectdailyreport.projectdailyreport.exception.DuplicateReportException;
import az.projectdailyreport.projectdailyreport.exception.ReportNotFoundException;
import az.projectdailyreport.projectdailyreport.model.DailyReport;
import az.projectdailyreport.projectdailyreport.model.Project;
import az.projectdailyreport.projectdailyreport.model.User;
import az.projectdailyreport.projectdailyreport.repository.DailyReportRepository;
import az.projectdailyreport.projectdailyreport.service.DailyReportService;
import az.projectdailyreport.projectdailyreport.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {


    private final DailyReportRepository dailyReportRepository;


    private final ProjectService projectService;
    LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
    LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);


    @Override
    public DailyReportDTO createDailyReport(DailyReportRequest dailyReportRequest, User user) {
        Project project = projectService.getProjectById(dailyReportRequest.getProjectId());
        if (dailyReportRepository.existsByProjectAndUserAndLocalDateTimeBetween(
                project, user, startOfDay, endOfDay)) {
            throw new DuplicateReportException("Aynı projeye ve kullanıcıya ait rapor zaten mevcut.");
        }

        DailyReport dailyReport = new DailyReport();
        dailyReport.setFirstName(user.getFirstName());
        dailyReport.setLastName(user.getLastName());
//        dailyReport.setLocalDateTime(LocalDateTime.now());
        LocalDateTime utcDateTime = LocalDateTime.now(ZoneOffset.UTC);
        dailyReport.setLocalDateTime(utcDateTime);
        dailyReport.setReportText(dailyReportRequest.getReportText());
        dailyReport.setUser(user);
        dailyReport.setProject(project);

        DailyReport savedReport = dailyReportRepository.save(dailyReport);
        ModelMapper modelMapper = new ModelMapper();

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);

        return   modelMapper.map(savedReport, DailyReportDTO.class);
    }

    @Override
    public DailyReportDTO updateDailyReport(Long reportId, DailyReportUpdate updatedReportText) {
        DailyReport existingReport = dailyReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Daily Report not found with id: " + reportId));

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        if (!existingReport.getLocalDateTime().isAfter(startOfDay) ||
                !existingReport.getLocalDateTime().isBefore(endOfDay)) {
            throw new DailyReportUpdateException("Günlük raporu sadece kaydedildiği gün içinde güncellenebilir.");
        }

        existingReport.setReportText(updatedReportText.getReportText());

        ModelMapper modelMapper =new ModelMapper();
        DailyReport updatedReport = dailyReportRepository.save(existingReport);

        return modelMapper.map(updatedReport, DailyReportDTO.class);
    }

    public List<DailyReportUser> getAllDailyReportsForUser(Long id) {
        List<DailyReport> dailyReports = dailyReportRepository.findByUser_Id(id);
        return mapToUserDTOList(dailyReports);
    }

    public List<DailyReportAdmin> getAllDailyReportsForAdmin() {
        List<DailyReport> dailyReports = dailyReportRepository.findAll();
        return mapToAdminDTOList(dailyReports);
    }

    private List<DailyReportUser> mapToUserDTOList(List<DailyReport> dailyReports) {


        return dailyReports.stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());

    }

    private List<DailyReportAdmin> mapToAdminDTOList(List<DailyReport> dailyReports) {
        return dailyReports.stream()
                .map(this::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    private DailyReportUser mapToUserDTO(DailyReport dailyReport) {
        return DailyReportUser.builder()
                .id(dailyReport.getId())
                .userId(dailyReport.getUser().getId())
                .localDateTime(dailyReport.getLocalDateTime())
                .reportText(dailyReport.getReportText())  // Raporu gizle, isteğe göre farklı bir değer de atanabilir
                .project(mapProjectToDTO(dailyReport.getProject()))
                // Diğer alanlar
                .build();
    }

    private DailyReportAdmin mapToAdminDTO(DailyReport dailyReport) {
        return DailyReportAdmin.builder()
                .id(dailyReport.getId())
                .userId(dailyReport.getUser().getId())
                .firstName(dailyReport.getFirstName())
                .lastName(dailyReport.getLastName())
                .localDateTime(dailyReport.getLocalDateTime())
                .reportText(dailyReport.getReportText())
                .project(mapProjectToDTO(dailyReport.getProject()))
                .build();
    }
    public ProjectDTO mapProjectToDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .build();
    }

    @Override
    public Page<DailyReportAdmin> getFilteredDailyReportsForAdmin(
            List<Long> userIds, LocalDate startDate, LocalDate endDate,
            List<Long> projectIds, Pageable pageable) {

        if (userIds==null  && startDate==null && endDate==null && projectIds==null){
            Page<DailyReport> getall = dailyReportRepository.findAll(pageable);
            return getall.map(this::mapToAdminDTO);
        }else {
        Page<DailyReport> filteredReports = dailyReportRepository.findByUserIdInAndLocalDateTimeBetweenAndProjectIdIn(
                userIds, startDate, endDate, projectIds, pageable);
        return filteredReports.map(this::mapToAdminDTO);}
    }
    @Override
    public List<DailyReportUser> getUserReportsBetweenDates(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        List<DailyReport> filteredReports = dailyReportRepository.findUserReportsBetweenDates(userId, startDate, endDate, pageable);
    return mapToUserDTOList(filteredReports);
    }

}