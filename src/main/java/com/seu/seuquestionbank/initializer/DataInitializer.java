package com.seu.seuquestionbank.initializer;

import com.seu.seuquestionbank.enums.CourseCategory;
import com.seu.seuquestionbank.enums.CourseType;
import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.model.Department;
import com.seu.seuquestionbank.model.Program;
import com.seu.seuquestionbank.repository.CourseRepository;
import com.seu.seuquestionbank.repository.DepartmentRepository;
import com.seu.seuquestionbank.repository.ProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes stable academic data: Program, Department, and Courses.
 * Idempotent - safe to run on every startup.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    public DataInitializer(ProgramRepository programRepository,
                           DepartmentRepository departmentRepository,
                           CourseRepository courseRepository) {
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initProgram();
        initDepartment();
        initCourses();
    }

    private void initProgram() {
        if (programRepository.existsByCode("CSE")) {
            log.info("Program CSE already exists, skipping");
            return;
        }
        Program program = Program.builder()
                .name("Computer Science and Engineering")
                .code("CSE")
                .degree("BSc")
                .description("BSc in Computer Science and Engineering, SEU. 4 years (min), 6 years (max), 150 credits, 64 courses, CGPA 2.5 minimum. Current batch: 65.")
                .active(true)
                .build();
        programRepository.save(program);
        log.info("Program CSE created");
    }

    private void initDepartment() {
        if (departmentRepository.existsByCode("CSE")) {
            log.info("Department CSE already exists, skipping");
            return;
        }
        Department dept = Department.builder()
                .name("Department of Computer Science and Engineering")
                .code("CSE")
                .active(true)
                .build();
        departmentRepository.save(dept);
        log.info("Department CSE created");
    }

    private void initCourses() {
        List<Course> curriculum = buildCurriculum();
        int inserted = 0;
        int skipped = 0;
        for (Course course : curriculum) {
            if (courseRepository.existsByCourseCode(course.getCourseCode())) {
                skipped++;
                continue;
            }
            courseRepository.save(course);
            inserted++;
        }
        log.info("Course initialization complete: inserted={}, skipped (already exists)={}, total curriculum={}", inserted, skipped, curriculum.size());
    }

    private List<Course> buildCurriculum() {
        List<Course> list = new ArrayList<>();

        // Helper: add courses with full control over fields

        // CSE courses
        list.add(course("CSE141", "Computer Fundamentals", 3, CourseCategory.CORE, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("CSE161", "Programming Language I", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE141"), null, null, "EEE133", "CSE"));
        list.add(course("CSE162", "Programming Language I Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE141"), null, null, "EEE134", "CSE"));
        list.add(course("CSE181", "Discrete Mathematics", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE161", "CSE162"), null, null, null, "CSE"));
        list.add(course("CSE241", "Data Structures", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE181"), null, null, null, "CSE"));
        list.add(course("CSE242", "Data Structures Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE181"), null, null, null, "CSE"));
        list.add(course("CSE261", "Numerical Methods", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE181", "MAT241"), null, null, null, "CSE"));
        list.add(course("CSE263", "Digital Logic Design", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE161"), null, null, null, "CSE"));
        list.add(course("CSE264", "Digital Logic Design Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE161"), null, null, null, "CSE"));
        list.add(course("CSE265", "Algorithm", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE241"), null, null, null, "CSE"));
        list.add(course("CSE266", "Algorithm Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE242", "CSE241"), null, null, null, "CSE"));
        list.add(course("CSE281", "Introduction to Programming Language II (Java)", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE161"), null, null, null, "CSE"));
        list.add(course("CSE282", "Introduction to Programming Language II (Java) Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE161", "CSE162"), null, null, null, "CSE"));
        list.add(course("CSE341", "Computer Networking", 3, CourseCategory.CORE, CourseType.THEORY, List.of("ETE281"), null, null, null, "CSE"));
        list.add(course("CSE342", "Computer Networking Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("ETE281"), null, null, null, "CSE"));
        list.add(course("CSE343", "Computer Architecture", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE263", "CSE241"), null, null, null, "CSE"));
        list.add(course("CSE345", "Information System Design & Software Engineering", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE282", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE346", "Information System Design & Software Engineering Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE282", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE347", "Advanced Networking", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE341", "CSE342"), null, null, null, "CSE"));
        list.add(course("CSE348", "Advanced Networking Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE341", "CSE342"), null, null, null, "CSE"));
        list.add(course("CSE349", "Mathematical Analysis for Computer Science (theory)", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE265", "STA281"), null, null, null, "CSE"));
        list.add(course("CSE351", "Advanced Java", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE281"), null, null, null, "CSE"));
        list.add(course("CSE352", "Advanced Java Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE281"), null, null, null, "CSE"));
        list.add(course("CSE353", "Introduction to Data Mining", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE265", "CSE383"), null, null, null, "CSE"));
        list.add(course("CSE355", "Image Processing", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE265", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE359", "Graph Theory", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE265"), null, null, null, "CSE"));
        list.add(course("CSE361", "Operating Systems", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE363"), null, null, null, "CSE"));
        list.add(course("CSE362", "Operating Systems Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE363", "CSE364"), null, null, null, "CSE"));
        list.add(course("CSE363", "Microprocessor Design & Assembly Language Programming", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE241"), null, null, null, "CSE"));
        list.add(course("CSE364", "Microprocessor Design & Assembly Language Programming Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE241"), null, null, null, "CSE"));
        list.add(course("CSE365", "Artificial Intelligence", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE265"), null, null, null, "CSE"));
        list.add(course("CSE375", "Advanced Algorithm", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE265"), null, null, null, "CSE"));
        list.add(course("CSE376", "Advanced Algorithm Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE265"), null, null, null, "CSE"));
        list.add(course("CSE381", "Introduction to Embedded Systems", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE363"), null, null, null, "CSE"));
        list.add(course("CSE382", "Introduction to Embedded Systems Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE364"), null, null, null, "CSE"));
        list.add(course("CSE383", "Database Design", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE241", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE384", "Database Design Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE242", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE441", "Theory of Computing", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE241"), null, null, null, "CSE"));
        list.add(course("CSE443", "Computer Graphics & Animation", 3, CourseCategory.CORE, CourseType.THEORY, List.of("CSE281", "MAT261"), null, null, null, "CSE"));
        list.add(course("CSE444", "Computer Graphics & Animation Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("CSE281", "MAT261"), null, null, null, "CSE"));
        list.add(course("CSE445", "Smart Device App Development", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE341", "CSE281"), null, null, null, "CSE"));
        list.add(course("CSE457", "Advanced Embedded Systems", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE381"), null, null, null, "CSE"));
        list.add(course("CSE458", "Advanced Embedded Systems Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE381"), null, null, null, "CSE"));
        list.add(course("CSE465", "Parallel and Distributed Computing", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE361", "CSE381"), null, null, null, "CSE"));
        list.add(course("CSE466", "Parallel and Distributed Computing Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE361", "CSE381"), null, null, null, "CSE"));
        list.add(course("CSE467", "E-Commerce & E-Governance", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE383", "SOC341"), null, null, null, "CSE"));
        list.add(course("CSE469", "Compiler Construction", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE441", "CSE282"), null, null, null, "CSE"));
        list.add(course("CSE471", "Web and Internet Programming", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE345", "CSE346"), null, null, null, "CSE"));
        list.add(course("CSE472", "Web and Internet Programming Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE345", "CSE346"), null, null, null, "CSE"));
        list.add(course("CSE473", "Software Development and Project Management", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE345"), null, null, null, "CSE"));
        list.add(course("CSE474", "Software Development and Project Management Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE345"), null, null, null, "CSE"));
        list.add(course("CSE475", "Management Information System", 3, CourseCategory.ELECTIVE, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("CSE477", "Cloud Computing", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE361", "CSE381"), null, null, null, "CSE"));
        list.add(course("CSE479", "Cryptography & Network Security", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE341"), null, null, null, "CSE"));
        list.add(course("CSE489", "Internship", 3, CourseCategory.PROJECT_INTERNSHIP, CourseType.THEORY, null, null, 100, null, "CSE"));
        list.add(course("CSE491", "Project", 3, CourseCategory.PROJECT_INTERNSHIP, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("CSE460", "Final Year Design Project I", 1, CourseCategory.RESEARCH_METHODOLOGY, CourseType.THEORY, null, null, 100, null, "CSE"));
        list.add(course("CSE461", "Final Year Design Project II", 1, CourseCategory.RESEARCH_METHODOLOGY, CourseType.THEORY, List.of("CSE460"), null, null, null, "CSE"));
        list.add(course("CSE462", "Final Year Design Project III", 1, CourseCategory.RESEARCH_METHODOLOGY, CourseType.THEORY, List.of("CSE461"), null, null, null, "CSE"));
        list.add(course("CSE437", "Machine Learning", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE365"), 5, 1, null, "CSE"));
        list.add(course("CSE438", "Machine Learning Lab", 1, CourseCategory.ELECTIVE, CourseType.PRACTICAL, List.of("CSE365"), 5, 1, null, "CSE"));

        // EEE courses
        list.add(course("EEE181", "Electrical Circuits Design I", 3, CourseCategory.CORE, CourseType.THEORY, List.of("EEE111"), null, null, null, "CSE"));
        list.add(course("EEE182", "Electrical Circuits Design I Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("EEE132"), null, null, null, "CSE"));
        list.add(course("EEE241", "Electronic Devices & Circuits I", 3, CourseCategory.CORE, CourseType.THEORY, List.of("EEE181"), null, null, null, "CSE"));
        list.add(course("EEE242", "Electronic Devices & Circuits I Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("EEE181"), null, null, null, "CSE"));
        list.add(course("EEE425", "VLSI Design", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE264", "CSE263"), null, null, null, "CSE"));
        list.add(course("EEE456", "VLSI Design Lab", 1, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("CSE264", "CSE263"), null, null, null, "CSE"));

        // ETE courses
        list.add(course("ETE281", "Communication Theory", 3, CourseCategory.CORE, CourseType.THEORY, List.of("MAT241", "EEE241"), null, null, null, "CSE"));
        list.add(course("ETE282", "Communication Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("MAT241", "EEE241"), null, null, null, "CSE"));
        list.add(course("ETE357", "Telecommunication Engineering", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("ETE281"), null, null, null, "CSE"));
        list.add(course("ETE358", "Telecommunication Engineering Lab", 1, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("ETE281"), null, null, null, "CSE"));
        list.add(course("ETE399", "Digital Communication", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("MAT261", "ETE281"), null, null, null, "CSE"));
        list.add(course("ETE451", "Digital Signal Processing", 3, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("ETE399", "ETE281"), null, null, null, "CSE"));
        list.add(course("ETE452", "Digital Signal Prosessing Lab", 1, CourseCategory.ELECTIVE, CourseType.THEORY, List.of("ETE399", "ETE281"), null, null, null, "CSE"));

        // Mathematics
        list.add(course("MAT009", "Remedial Mathematics", 0, CourseCategory.REMEDIAL_NON_CREDIT, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("MAT141", "Differential & Integral Calculus", 3, CourseCategory.CORE, CourseType.THEORY, List.of("MAT111"), null, null, null, "CSE"));
        list.add(course("MAT161", "Coordinate Geometry and Vector Analysis", 3, CourseCategory.CORE, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("MAT241", "Complex Variables and Transforms (Laplace & Fourier)", 3, CourseCategory.CORE, CourseType.THEORY, List.of("MAT141"), null, null, "MAT135", "CSE"));
        list.add(course("MAT261", "Linear Algebra and Matrices", 3, CourseCategory.CORE, CourseType.THEORY, List.of("MAT241"), null, null, null, "CSE"));

        // Physics
        list.add(course("PHY161", "Physics I", 3, CourseCategory.CORE, CourseType.THEORY, List.of("PHY115"), null, null, null, "CSE"));
        list.add(course("PHY181", "Physics II", 3, CourseCategory.CORE, CourseType.THEORY, List.of("PHY161"), null, null, "PHY121", "CSE"));
        list.add(course("PHY182", "Physics II Lab", 1, CourseCategory.CORE, CourseType.PRACTICAL, List.of("PHY161"), null, null, "PHY122", "CSE"));

        // English
        list.add(course("ENG101", "Basic English Skills", 3, CourseCategory.CORE, CourseType.ENGLISH, null, null, null, null, "CSE"));
        list.add(course("ENG102", "Intermediate English Skills", 3, CourseCategory.CORE, CourseType.ENGLISH, List.of("ENG101"), null, null, null, "CSE"));
        list.add(course("ENG103", "Advanced English Skills", 3, CourseCategory.CORE, CourseType.ENGLISH, List.of("ENG102"), null, null, null, "CSE"));
        list.add(course("ENG105", "Public Speaking", 3, CourseCategory.CORE, CourseType.ENGLISH, List.of("ENG103"), null, null, null, "CSE"));

        // General
        list.add(course("ACT141", "Introduction to Accounting", 3, CourseCategory.GENERAL, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("MGT281", "Introduction to Business & Management", 3, CourseCategory.GENERAL, CourseType.THEORY, null, null, null, null, "CSE"));
        list.add(course("SOC341", "Engineering Ethics", 3, CourseCategory.GENERAL, CourseType.THEORY, null, null, 50, null, "CSE"));

        // Statistics
        list.add(course("STA281", "Statistical Methods & Probability", 3, CourseCategory.CORE, CourseType.THEORY, null, null, null, null, "CSE"));

        return list;
    }

    private Course course(String code, String title, double credit, CourseCategory category, CourseType type,
                          List<String> prereqs, Integer minSem, Integer reqCredits, String alternate, String programCode) {
        return Course.builder()
                .courseCode(code)
                .title(title)
                .credit(credit)
                .category(category)
                .courseType(type)
                .prerequisiteCodes(prereqs == null ? new ArrayList<>() : new ArrayList<>(prereqs))
                .minimumSemester(minSem)
                .requiredCredits(reqCredits)
                .alternateCourseCode(alternate)
                .programCode(programCode)
                .active(true)
                .build();
    }
}
