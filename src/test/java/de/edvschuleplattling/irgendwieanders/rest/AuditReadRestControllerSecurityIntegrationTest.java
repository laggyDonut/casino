package de.edvschuleplattling.irgendwieanders.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditReadRestControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuditLogs_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/api/admin/audit")
                        .with(auth("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLogs_gamerRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/audit")
                        .with(auth("GAMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportAuditLogsCsv_gamerRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/audit/export")
                        .with(auth("GAMER")))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor auth(String authority) {
        return SecurityMockMvcRequestPostProcessors.user("test-" + authority.toLowerCase())
                .authorities(new SimpleGrantedAuthority(authority));
    }
}
