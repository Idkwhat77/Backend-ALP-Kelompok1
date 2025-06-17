package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.entity.Company;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.Skill;
import com.ruangkerja.rest.repository.CompanyRepository;
import com.ruangkerja.rest.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HomePageSearchController {
    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;

    @GetMapping("/search")
    public List<Map<String, Object>> searchAll(@RequestParam String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        String q = query.toLowerCase();

        // Search Company
        for (Company c : companyRepository.findAll()) {
            boolean match =
                (c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(q)) ||
                (c.getIndustry() != null && c.getIndustry().toLowerCase().contains(q)) ||
                (c.getHq() != null && c.getHq().toLowerCase().contains(q)) ||
                (c.getDescription() != null && c.getDescription().toLowerCase().contains(q)) ||
                (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)) ||
                (c.getCompanySize() != null && String.valueOf(c.getCompanySize()).contains(q)) ||
                (c.getFoundationDate() != null && c.getFoundationDate().toString().contains(q));
            // Tambahkan field lain jika ada

            if (match) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "company");
                map.put("name", c.getCompanyName());
                map.put("location", c.getHq());
                // Tambahkan field lain jika ingin ditampilkan
                results.add(map);
            }
        }

        // Search Candidate
        for (Candidate cand : candidateRepository.findAll()) {
            boolean skillMatch = false;
            if (cand.getSkill() != null) {
                for (Skill skill : cand.getSkill()) {
                    if (skill != null && skill.getName() != null && skill.getName().toLowerCase().contains(q)) {
                        skillMatch = true;
                        break;
                    }
                }
            }
            boolean match =
                (cand.getFullName() != null && cand.getFullName().toLowerCase().contains(q)) ||
                (cand.getBiodata() != null && cand.getBiodata().toLowerCase().contains(q)) ||
                (cand.getEmail() != null && cand.getEmail().toLowerCase().contains(q)) ||
                (cand.getCity() != null && cand.getCity().toLowerCase().contains(q)) ||
                (cand.getJobType() != null && cand.getJobType().toLowerCase().contains(q)) ||
                (cand.getIndustry() != null && cand.getIndustry().toLowerCase().contains(q)) ||
                (cand.getEmploymentStatus() != null && cand.getEmploymentStatus().toLowerCase().contains(q)) ||
                skillMatch;
            // Tambahkan field lain jika ada

            if (match) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "candidate");
                map.put("fullName", cand.getFullName());
                map.put("location", cand.getCity());
                // Tambahkan field lain jika ingin ditampilkan
                results.add(map);
            }
        }

        return results;
    }
}
