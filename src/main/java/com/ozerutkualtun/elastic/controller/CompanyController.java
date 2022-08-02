package com.ozerutkualtun.elastic.controller;

import com.ozerutkualtun.elastic.model.Company;
import com.ozerutkualtun.elastic.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.Operator;
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
@Log4j2
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
        log.info("Company saving with name: {}", company.getName());
        return companyRepository.save(company);
    }

    @GetMapping("/search")
    public List<SearchHit<Company>> searchCompanies(@RequestParam String searchTerm) {

        log.info("Searching for {}", searchTerm);

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("description", searchTerm).operator(Operator.AND)) // operator(Operator.AND)) kısmı full text search için -> exact match'de tek sonuç döndürür.
                .build();

        return elasticsearchOperations.search(searchQuery, Company.class, IndexCoordinates.of(companyIndexCoordinates)).getSearchHits();
    }


    /*
    Kullanıcılar arama yaparken yazım hataları yapabilirler. Bu tür bir senaryoyu ele almak için fuziness kavramı kullanılmaktadır.
    Böylelikle arama için gönderilen text, kayıtlarla tamamen eşleşmiyorsa bile yine de bir sonuç üretmek mümkün olmaktadır.

     */
    @GetMapping("/fuzzy-search")
    public List<SearchHit<Company>> getCompaniesByFuzzyDescription(@RequestParam String searchTerm) {

        log.info("Fuzzy searching for {}", searchTerm);

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("description", searchTerm)
                        .operator(Operator.AND)
                        .fuzziness(Fuzziness.ONE) // tek yanlış harfi tolere ediyor.
                        .prefixLength(2))
                .build();
        return elasticsearchOperations.search(searchQuery, Company.class,
                IndexCoordinates.of(companyIndexCoordinates)).getSearchHits();
    }

}
