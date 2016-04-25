package com.ali.web.rest;

import com.codahale.metrics.annotation.Timed;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import com.ali.domain.Student;
import com.ali.service.StudentService;
import com.ali.web.rest.util.HeaderUtil;
import com.ali.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Student.
 */
@RestController
@RequestMapping("/api")
public class StudentResource {

    private final Logger log = LoggerFactory.getLogger(StudentResource.class);
        
    @Inject
    private StudentService studentService;
    
    /**
     * POST  /students : Create a new student.
     *
     * @param student the student to create
     * @return the ResponseEntity with status 201 (Created) and with body the new student, or with status 400 (Bad Request) if the student has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/students",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Student> createStudent(@RequestBody Student student) throws URISyntaxException {
        log.debug("REST request to save Student : {}", student);
        if (student.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("student", "idexists", "A new student cannot already have an ID")).body(null);
        }
        Student result = studentService.save(student);
        return ResponseEntity.created(new URI("/api/students/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("student", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /students : Updates an existing student.
     *
     * @param student the student to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated student,
     * or with status 400 (Bad Request) if the student is not valid,
     * or with status 500 (Internal Server Error) if the student couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/students",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Student> updateStudent(@RequestBody Student student) throws URISyntaxException {
        log.debug("REST request to update Student : {}", student);
        if (student.getId() == null) {
            return createStudent(student);
        }
        Student result = studentService.save(student);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("student", student.getId().toString()))
            .body(result);
    }

    /**
     * GET  /students : get all the students.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of students in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/students",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Student>> getAllStudents(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Students");
        Page<Student> page = studentService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/students");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /students/:id : get the "id" student.
     *
     * @param id the id of the student to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the student, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/students/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        log.debug("REST request to get Student : {}", id);
        Student student = studentService.findOne(id);
        return Optional.ofNullable(student)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /students/:id : delete the "id" student.
     *
     * @param id the id of the student to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/students/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        log.debug("REST request to delete Student : {}", id);
        studentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("student", id.toString())).build();
    }
    
    
    @SuppressWarnings("deprecation")
	@RequestMapping(value = "/exportPDF",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
        @Timed
        public void exportPDF() throws IOException {
            log.debug("Make Student PDF Report");
            Pageable pageable = new PageRequest(0, 10);
            Page<Student> students=studentService.findAll(pageable);
          
            List<Student> list=students.getContent();
    
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream inputStream = new FileInputStream (classLoader.getResource("jasper/jasperview.jrxml").getPath());
            
            JRDataSource beanColDataSource = new JRBeanCollectionDataSource(list);
             
            JasperDesign jasperDesign;
			try {
				Map<String,Object> parameters = new HashMap<String,Object>();
				
				 jasperDesign = JRXmlLoader.load(inputStream);

				JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,beanColDataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, "C:\\pdfReport.pdf"); 
			} catch (JRException e) {
				e.printStackTrace();
			}
   
        }
  
    @SuppressWarnings("deprecation")
   	@RequestMapping(value = "/exportWord",
               method = RequestMethod.GET,
               produces = MediaType.APPLICATION_JSON_VALUE)
           @Timed
           public void exportWord() throws FileNotFoundException {
               log.debug("Make Student word Report");
               Pageable pageable = new PageRequest(0, 10);
               Page<Student> students=studentService.findAll(pageable);
             
               List<Student> list=students.getContent();
               ClassLoader classLoader = getClass().getClassLoader();

               InputStream inputStream = new FileInputStream (classLoader.getResource("jasper/jasperview.jrxml").getPath());
               
               JRDataSource beanColDataSource = new JRBeanCollectionDataSource(list);
                
               JasperDesign jasperDesign;
   			try {
   			     Map<String,Object> parameters = new HashMap<String,Object>();
   				 jasperDesign = JRXmlLoader.load(inputStream);
                 JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                 JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,beanColDataSource);               
               
               final JRRtfExporter rtfExporter = new JRRtfExporter();
               
                //File destFile = new File("/home/ali/reports/wordReport.rtf");
                File destFile = new File("C:\\wordReport.rtf");

               rtfExporter.setParameter(JRExporterParameter.JASPER_PRINT,jasperPrint);
               rtfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,destFile.toString());
               rtfExporter.exportReport();
               
   			} catch (JRException e) {
   				e.printStackTrace();
   			}
      
           }
    
    @SuppressWarnings("deprecation")
   	@RequestMapping(value = "/exportHTML",
               method = RequestMethod.GET,
               produces = MediaType.APPLICATION_JSON_VALUE)
           @Timed
           public void exportHTML() throws FileNotFoundException {
               log.debug("Make Student word Report");
               Pageable pageable = new PageRequest(0, 10);
               Page<Student> students=studentService.findAll(pageable);
             
               List<Student> list=students.getContent();
               ClassLoader classLoader = getClass().getClassLoader();

               InputStream inputStream = new FileInputStream (classLoader.getResource("jasper/jasperview.jrxml").getPath());
               
               JRDataSource beanColDataSource = new JRBeanCollectionDataSource(list);
                
               JasperDesign jasperDesign;
   			try {
   			     Map<String,Object> parameters = new HashMap<String,Object>();
   				 jasperDesign = JRXmlLoader.load(inputStream);
                 JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                 JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,beanColDataSource);               
               
               final JRRtfExporter rtfExporter = new JRRtfExporter();
               //File destFile = new File("/home/ali/reports/HTMLReport.html");
               File destFile = new File("C:\\HTMLReport.html");

               rtfExporter.setParameter(JRExporterParameter.JASPER_PRINT,jasperPrint);
               rtfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,destFile.toString());
               rtfExporter.exportReport();
               
   			} catch (JRException e) {
   				e.printStackTrace();
   			}
      
           }
    

}
