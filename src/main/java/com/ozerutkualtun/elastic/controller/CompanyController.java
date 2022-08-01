package com.ozerutkualtun.elastic.controller;

import com.ozerutkualtun.elastic.model.Company;
import com.ozerutkualtun.elastic.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    @Value("${index.coordinates.company}")
    private String companyIndexCoordinates;

    private final CompanyRepository companyRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @GetMapping
    public List<Company> getCompanyByName(@RequestParam String name) {

        return companyRepository.findByName(name, PageRequest.of(0, 20)).getContent();
    }

    @PostMapping
    public Company saveCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }

    @GetMapping("/search")
    public List<SearchHit<Company>> searchCompanies(@RequestParam String searchTerm) {

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("description", searchTerm))
                .build();

        return elasticsearchOperations.search(searchQuery, Company.class, IndexCoordinates.of(companyIndexCoordinates)).getSearchHits();
    }

}
