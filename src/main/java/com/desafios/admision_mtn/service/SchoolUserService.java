package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.CreateSchoolUserDto;
import com.desafios.admision_mtn.dto.SchoolUserResponseDto;
import com.desafios.admision_mtn.model.*;
import com.desafios.admision_mtn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SchoolUserService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ProfessorRepository professorRepository;
    
    @Autowired
    private KinderTeacherRepository kinderTeacherRepository;
    
    @Autowired
    private PsychologistRepository psychologistRepository;
    
    @Autowired
    private SupportStaffRepository supportStaffRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public SchoolUserResponseDto createSchoolUser(CreateSchoolUserDto dto) {
        // Validar email único
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya existe: " + dto.getEmail());
        }

        // Crear usuario base
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getEmail()); // Usar email como username
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(dto.getRole());
        usuario.setFirstName(dto.getFirstName());
        usuario.setLastName(dto.getLastName());
        usuario.setPhone(dto.getPhone());
        usuario.setActive(true);
        usuario.setEmailVerified(true); // Para personal del colegio, automáticamente verificado
        
        usuario = usuarioRepository.save(usuario);

        // Crear registro específico según el rol
        switch (dto.getRole()) {
            case PROFESSOR:
                createProfessor(usuario, dto);
                break;
            case KINDER_TEACHER:
                createKinderTeacher(usuario, dto);
                break;
            case PSYCHOLOGIST:
                createPsychologist(usuario, dto);
                break;
            case SUPPORT_STAFF:
                createSupportStaff(usuario, dto);
                break;
            default:
                throw new RuntimeException("Rol no válido para personal del colegio: " + dto.getRole());
        }

        return convertToResponseDto(usuario);
    }

    private void createProfessor(Usuario usuario, CreateSchoolUserDto dto) {
        Professor professor = new Professor();
        professor.setUsuario(usuario);
        professor.setSubjects(dto.getSubjects() != null ? dto.getSubjects() : new ArrayList<>());
        professor.setAssignedGrades(dto.getAssignedGrades() != null ? dto.getAssignedGrades() : new ArrayList<>());
        professor.setDepartment(dto.getDepartment());
        professor.setYearsOfExperience(dto.getYearsOfExperience());
        professor.setQualifications(dto.getQualifications() != null ? dto.getQualifications() : new ArrayList<>());
        professor.setAdmin(false);
        
        professorRepository.save(professor);
    }

    private void createKinderTeacher(Usuario usuario, CreateSchoolUserDto dto) {
        KinderTeacher teacher = new KinderTeacher();
        teacher.setUsuario(usuario);
        teacher.setAssignedLevel(dto.getAssignedLevel());
        teacher.setSpecializations(dto.getSpecializations() != null ? dto.getSpecializations() : new ArrayList<>());
        teacher.setYearsOfExperience(dto.getYearsOfExperience());
        teacher.setQualifications(dto.getQualifications() != null ? dto.getQualifications() : new ArrayList<>());
        
        kinderTeacherRepository.save(teacher);
    }

    private void createPsychologist(Usuario usuario, CreateSchoolUserDto dto) {
        Psychologist psychologist = new Psychologist();
        psychologist.setUsuario(usuario);
        psychologist.setSpecialty(dto.getSpecialty());
        psychologist.setLicenseNumber(dto.getLicenseNumber());
        psychologist.setAssignedGrades(dto.getAssignedGrades() != null ? dto.getAssignedGrades() : new ArrayList<>());
        psychologist.setCanConductInterviews(dto.getCanConductInterviews() != null ? dto.getCanConductInterviews() : false);
        psychologist.setCanPerformPsychologicalEvaluations(dto.getCanPerformPsychologicalEvaluations() != null ? dto.getCanPerformPsychologicalEvaluations() : false);
        psychologist.setSpecializedAreas(dto.getSpecializedAreas() != null ? dto.getSpecializedAreas() : new ArrayList<>());
        
        psychologistRepository.save(psychologist);
    }

    private void createSupportStaff(Usuario usuario, CreateSchoolUserDto dto) {
        SupportStaff staff = new SupportStaff();
        staff.setUsuario(usuario);
        staff.setStaffType(dto.getStaffType());
        staff.setDepartment(dto.getDepartment());
        staff.setResponsibilities(dto.getResponsibilities() != null ? dto.getResponsibilities() : new ArrayList<>());
        staff.setCanAccessReports(dto.getCanAccessReports() != null ? dto.getCanAccessReports() : false);
        staff.setCanManageSchedules(dto.getCanManageSchedules() != null ? dto.getCanManageSchedules() : false);
        
        supportStaffRepository.save(staff);
    }

    public List<SchoolUserResponseDto> getAllSchoolUsers() {
        List<Usuario> schoolUsers = usuarioRepository.findByRolIn(
            List.of(RolUsuario.PROFESSOR, RolUsuario.KINDER_TEACHER, RolUsuario.PSYCHOLOGIST, RolUsuario.SUPPORT_STAFF)
        );
        
        return schoolUsers.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<SchoolUserResponseDto> getActiveSchoolUsers() {
        List<Usuario> activeUsers = usuarioRepository.findByRolInAndIsActiveTrue(
            List.of(RolUsuario.PROFESSOR, RolUsuario.KINDER_TEACHER, RolUsuario.PSYCHOLOGIST, RolUsuario.SUPPORT_STAFF)
        );
        
        return activeUsers.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<SchoolUserResponseDto> getUsersByRole(RolUsuario role) {
        List<Usuario> users = usuarioRepository.findByRolAndIsActiveTrue(role);
        return users.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public Optional<SchoolUserResponseDto> getUserById(Long id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::isSchoolStaff)
                .map(this::convertToResponseDto);
    }

    public SchoolUserResponseDto updateUser(Long id, CreateSchoolUserDto dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.isSchoolStaff()) {
            throw new RuntimeException("Usuario no es personal del colegio");
        }

        // Actualizar datos base
        usuario.setFirstName(dto.getFirstName());
        usuario.setLastName(dto.getLastName());
        usuario.setPhone(dto.getPhone());
        
        usuario = usuarioRepository.save(usuario);

        // Actualizar datos específicos según rol
        updateRoleSpecificData(usuario, dto);

        return convertToResponseDto(usuario);
    }

    private void updateRoleSpecificData(Usuario usuario, CreateSchoolUserDto dto) {
        switch (usuario.getRol()) {
            case PROFESSOR:
                updateProfessor(usuario.getId(), dto);
                break;
            case KINDER_TEACHER:
                updateKinderTeacher(usuario.getId(), dto);
                break;
            case PSYCHOLOGIST:
                updatePsychologist(usuario.getId(), dto);
                break;
            case SUPPORT_STAFF:
                updateSupportStaff(usuario.getId(), dto);
                break;
        }
    }

    private void updateProfessor(Long id, CreateSchoolUserDto dto) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor no encontrado"));
        
        if (dto.getSubjects() != null) professor.setSubjects(dto.getSubjects());
        if (dto.getAssignedGrades() != null) professor.setAssignedGrades(dto.getAssignedGrades());
        if (dto.getDepartment() != null) professor.setDepartment(dto.getDepartment());
        if (dto.getYearsOfExperience() != null) professor.setYearsOfExperience(dto.getYearsOfExperience());
        if (dto.getQualifications() != null) professor.setQualifications(dto.getQualifications());
        
        professorRepository.save(professor);
    }

    private void updateKinderTeacher(Long id, CreateSchoolUserDto dto) {
        KinderTeacher teacher = kinderTeacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KinderTeacher no encontrado"));
        
        if (dto.getAssignedLevel() != null) teacher.setAssignedLevel(dto.getAssignedLevel());
        if (dto.getSpecializations() != null) teacher.setSpecializations(dto.getSpecializations());
        if (dto.getYearsOfExperience() != null) teacher.setYearsOfExperience(dto.getYearsOfExperience());
        if (dto.getQualifications() != null) teacher.setQualifications(dto.getQualifications());
        
        kinderTeacherRepository.save(teacher);
    }

    private void updatePsychologist(Long id, CreateSchoolUserDto dto) {
        Psychologist psychologist = psychologistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Psychologist no encontrado"));
        
        if (dto.getSpecialty() != null) psychologist.setSpecialty(dto.getSpecialty());
        if (dto.getLicenseNumber() != null) psychologist.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getAssignedGrades() != null) psychologist.setAssignedGrades(dto.getAssignedGrades());
        if (dto.getCanConductInterviews() != null) psychologist.setCanConductInterviews(dto.getCanConductInterviews());
        if (dto.getCanPerformPsychologicalEvaluations() != null) psychologist.setCanPerformPsychologicalEvaluations(dto.getCanPerformPsychologicalEvaluations());
        if (dto.getSpecializedAreas() != null) psychologist.setSpecializedAreas(dto.getSpecializedAreas());
        
        psychologistRepository.save(psychologist);
    }

    private void updateSupportStaff(Long id, CreateSchoolUserDto dto) {
        SupportStaff staff = supportStaffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupportStaff no encontrado"));
        
        if (dto.getStaffType() != null) staff.setStaffType(dto.getStaffType());
        if (dto.getDepartment() != null) staff.setDepartment(dto.getDepartment());
        if (dto.getResponsibilities() != null) staff.setResponsibilities(dto.getResponsibilities());
        if (dto.getCanAccessReports() != null) staff.setCanAccessReports(dto.getCanAccessReports());
        if (dto.getCanManageSchedules() != null) staff.setCanManageSchedules(dto.getCanManageSchedules());
        
        supportStaffRepository.save(staff);
    }

    public void deactivateUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActive(false);
        usuarioRepository.save(usuario);
    }

    public void reactivateUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActive(true);
        usuarioRepository.save(usuario);
    }

    public void deleteUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        // Eliminar datos específicos según el rol
        switch (usuario.getRol()) {
            case PROFESSOR:
                professorRepository.deleteById(id);
                break;
            case KINDER_TEACHER:
                kinderTeacherRepository.deleteById(id);
                break;
            case PSYCHOLOGIST:
                psychologistRepository.deleteById(id);
                break;
            case SUPPORT_STAFF:
                supportStaffRepository.deleteById(id);
                break;
        }
        
        // Eliminar el usuario base
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void updateSubjectsMapping() {
        // Obtener todas las materias existentes
        List<Professor> professors = professorRepository.findAll();
        
        for (Professor professor : professors) {
            if (professor.getSubjects() != null) {
                List<Professor.Subject> updatedSubjects = new ArrayList<>();
                
                for (Professor.Subject subject : professor.getSubjects()) {
                    // Convertir nombres en español a valores del enum
                    String subjectName = subject.name();
                    Professor.Subject mappedSubject = null;
                    
                    switch (subjectName.toUpperCase()) {
                        case "MATEMATICA":
                        case "MATEMÁTICA":
                        case "MATH":
                            mappedSubject = Professor.Subject.MATH;
                            break;
                        case "LENGUAJE":
                        case "LENGUA":
                        case "SPANISH":
                            mappedSubject = Professor.Subject.SPANISH;
                            break;
                        case "INGLES":
                        case "INGLÉS":
                        case "ENGLISH":
                            mappedSubject = Professor.Subject.ENGLISH;
                            break;
                        default:
                            // Si ya es un valor válido del enum, mantenerlo
                            if (subjectName.equals("MATH") || subjectName.equals("SPANISH") || subjectName.equals("ENGLISH")) {
                                mappedSubject = subject;
                            }
                            break;
                    }
                    
                    if (mappedSubject != null && !updatedSubjects.contains(mappedSubject)) {
                        updatedSubjects.add(mappedSubject);
                    }
                }
                
                // Actualizar las materias del profesor
                professor.setSubjects(updatedSubjects);
                professorRepository.save(professor);
            }
        }
    }

    private SchoolUserResponseDto convertToResponseDto(Usuario usuario) {
        SchoolUserResponseDto dto = new SchoolUserResponseDto();
        dto.setId(usuario.getId());
        dto.setFirstName(usuario.getFirstName());
        dto.setLastName(usuario.getLastName());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getRol());
        dto.setPhone(usuario.getPhone());
        dto.setActive(usuario.isActive());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        dto.setUpdatedAt(usuario.getUpdatedAt());

        // Cargar datos específicos según el rol
        loadRoleSpecificData(usuario, dto);

        return dto;
    }

    private void loadRoleSpecificData(Usuario usuario, SchoolUserResponseDto dto) {
        switch (usuario.getRol()) {
            case PROFESSOR:
                loadProfessorData(usuario.getId(), dto);
                break;
            case KINDER_TEACHER:
                loadKinderTeacherData(usuario.getId(), dto);
                break;
            case PSYCHOLOGIST:
                loadPsychologistData(usuario.getId(), dto);
                break;
            case SUPPORT_STAFF:
                loadSupportStaffData(usuario.getId(), dto);
                break;
        }
    }

    private void loadProfessorData(Long id, SchoolUserResponseDto dto) {
        professorRepository.findById(id).ifPresent(professor -> {
            dto.setSubjects(professor.getSubjects());
            dto.setAssignedGrades(professor.getAssignedGrades());
            dto.setDepartment(professor.getDepartment());
            dto.setYearsOfExperience(professor.getYearsOfExperience());
            dto.setQualifications(professor.getQualifications());
            dto.setIsAdmin(professor.isAdmin());
        });
    }

    private void loadKinderTeacherData(Long id, SchoolUserResponseDto dto) {
        kinderTeacherRepository.findById(id).ifPresent(teacher -> {
            dto.setAssignedLevel(teacher.getAssignedLevel());
            dto.setSpecializations(teacher.getSpecializations());
            dto.setYearsOfExperience(teacher.getYearsOfExperience());
            dto.setQualifications(teacher.getQualifications());
        });
    }

    private void loadPsychologistData(Long id, SchoolUserResponseDto dto) {
        psychologistRepository.findById(id).ifPresent(psychologist -> {
            dto.setSpecialty(psychologist.getSpecialty());
            dto.setLicenseNumber(psychologist.getLicenseNumber());
            dto.setAssignedGrades(psychologist.getAssignedGrades());
            dto.setCanConductInterviews(psychologist.isCanConductInterviews());
            dto.setCanPerformPsychologicalEvaluations(psychologist.isCanPerformPsychologicalEvaluations());
            dto.setSpecializedAreas(psychologist.getSpecializedAreas());
        });
    }

    private void loadSupportStaffData(Long id, SchoolUserResponseDto dto) {
        supportStaffRepository.findById(id).ifPresent(staff -> {
            dto.setStaffType(staff.getStaffType());
            dto.setDepartment(staff.getDepartment());
            dto.setResponsibilities(staff.getResponsibilities());
            dto.setCanAccessReports(staff.isCanAccessReports());
            dto.setCanManageSchedules(staff.isCanManageSchedules());
        });
    }
}