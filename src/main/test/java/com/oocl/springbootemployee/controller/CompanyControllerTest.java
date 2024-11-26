package com.oocl.springbootemployee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

import com.oocl.springbootemployee.model.Company;
import com.oocl.springbootemployee.model.Employee;
import com.oocl.springbootemployee.model.Gender;
import com.oocl.springbootemployee.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest({CompanyController.class, CompanyRepository.class})
@AutoConfigureJsonTesters
public class CompanyControllerTest {
    @Autowired
    private MockMvc client;

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JacksonTester<List<Company>> companyListJacksonTester;
    @Autowired
    private JacksonTester<List<Employee>> employeeListJacksonTester;

    @BeforeEach
    void setUp() {
        companyRepository.getAll().clear();
        companyRepository.addCompany(new Company(1, "Acme Corporation", List.of(
            new Employee(1, "John Smith", 32, Gender.MALE, 5000.0),
            new Employee(2, "Jane Johnson", 28, Gender.FEMALE, 6000.0)
        )));
        companyRepository.addCompany(new Company(2, "TechCom Solutions", List.of(
            new Employee(3, "David Williams", 35, Gender.MALE, 5500.0),
            new Employee(4, "Emily Brown", 23, Gender.FEMALE, 4500.0),
            new Employee(5, "Michael Jones", 40, Gender.MALE, 7000.0)
        )));
        companyRepository.addCompany(new Company("Global Innovators"));
        companyRepository.addCompany(new Company("Stellar Enterprises"));
        companyRepository.addCompany(new Company("Nexus Industries"));
    }

    @Test
    void should_return_all_companies() throws Exception {
        // Given
        final List<Company> givenCompanies = companyRepository.getAll();

        // When
        final MvcResult result = client.perform(MockMvcRequestBuilders.get("/companies")).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        final List<Company> fetchedCompanies =
            companyListJacksonTester.parseObject(result.getResponse().getContentAsString());
        assertThat(fetchedCompanies).hasSameSizeAs(givenCompanies);
        for (int i = 0; i < fetchedCompanies.size(); i++) {
            final Company fetchedCompany = fetchedCompanies.get(i);
            final Company givenCompany = givenCompanies.get(i);
            assertThat(fetchedCompany.getId()).isEqualTo(givenCompany.getId());
            assertThat(fetchedCompany.getName()).isEqualTo(givenCompany.getName());
            assertThat(fetchedCompany.getEmployees()).hasSize(givenCompany.getEmployees().size());
            for (int j = 0; j < fetchedCompany.getEmployees().size(); j++) {
                final Employee fetchedEmployee = fetchedCompany.getEmployees().get(i);
                final Employee givenEmployee = givenCompany.getEmployees().get(i);
                assertThat(fetchedEmployee.getId()).isEqualTo(givenEmployee.getId());
                assertThat(fetchedEmployee.getName()).isEqualTo(givenEmployee.getName());
                assertThat(fetchedEmployee.getAge()).isEqualTo(givenEmployee.getAge());
                assertThat(fetchedEmployee.getGender()).isEqualTo(givenEmployee.getGender());
                assertThat(fetchedEmployee.getSalary()).isEqualTo(givenEmployee.getSalary());
            }
        }
    }

    @Test
    void should_return_paged_companies_when_get_by_page_params() throws Exception {
        // Given
        var pageIndex = 3;
        var pageSize = 2;
        final var the5thEmployeeCompanyInPage3 = companyRepository.getCompanyById(5);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get(String.format("/companies?pageIndex=%s&pageSize=%s", pageIndex, pageSize)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(the5thEmployeeCompanyInPage3.getId()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(the5thEmployeeCompanyInPage3.getName()));
    }

    @Test
    void should_return_employees_when_get_employees_under_the_company() throws Exception {
        // Given
        var givenCompanyId = 1;

        // When
        final var result =
            client.perform(MockMvcRequestBuilders.get("/companies/" + givenCompanyId + "/employees")).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        final List<Employee> fetchedEmployees =
            employeeListJacksonTester.parseObject(result.getResponse().getContentAsString());
        final List<Employee> givenEmployees = companyRepository.getCompanyById(givenCompanyId).getEmployees();
        for (int i = 0; i < fetchedEmployees.size(); i++) {
            final var fetchedEmployee = fetchedEmployees.get(i);
            final var givenEmployee = givenEmployees.get(i);
            assertThat(fetchedEmployee.getId()).isEqualTo(givenEmployee.getId());
            assertThat(fetchedEmployee.getName()).isEqualTo(givenEmployee.getName());
            assertThat(fetchedEmployee.getAge()).isEqualTo(givenEmployee.getAge());
            assertThat(fetchedEmployee.getGender()).isEqualTo(givenEmployee.getGender());
            assertThat(fetchedEmployee.getSalary()).isEqualTo(givenEmployee.getSalary());
        }
    }

    @Test
    void should_return_company_when_get_by_id() throws Exception {
        // Given
        var companyId = 1;
        final var companyGiven = companyRepository.getCompanyById(companyId);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get("/companies/" + companyId))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(companyGiven.getName()));
    }

    @Test
    void should_return_created_company() throws Exception {
        //Given
        var givenName = "New Company";
        String requestBody = String.format("{\"name\": \"%s\" }", givenName);

        // When
        // Then
        client.perform(
                MockMvcRequestBuilders.post("/companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(6))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(givenName));
    }

    @Test
    void should_return_updated_company_when_update_with_id_and_data() throws Exception {
        // Given
        var idToUpdate = 1;
        var nameToUpdate = "New Name";
        String requestBody = String.format("{\"name\": \"%s\" }", nameToUpdate);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.put("/companies/" + idToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToUpdate))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(nameToUpdate));
    }

    @Test
    void should_return_no_content_when_delete() throws Exception {
        // Given
        var toDeleteEmployeeId = 1;

        // When
        final var result =
            client.perform(MockMvcRequestBuilders.delete("/companies/" + toDeleteEmployeeId)).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(companyRepository.getAll()).hasSize(4);
    }
}
