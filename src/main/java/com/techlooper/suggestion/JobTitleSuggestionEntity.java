package com.techlooper.suggestion;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Document(indexName = "suggester", type = "jobTitle")
public class JobTitleSuggestionEntity {

    @Id
    private String jobTitleName;

    @CompletionField(payloads = true, maxInputLength = 50, indexAnalyzer = "index_analyzer", searchAnalyzer = "search_analyzer")
    private Completion jobTitleNameSuggest;

    public JobTitleSuggestionEntity(String jobTitleName) {
        this.jobTitleName = jobTitleName;
    }

    public String getJobTitleName() {
        return jobTitleName;
    }

    public void setJobTitleName(String jobTitleName) {
        this.jobTitleName = jobTitleName;
    }

    public Completion getJobTitleNameSuggest() {
        return jobTitleNameSuggest;
    }

    public void setJobTitleNameSuggest(Completion jobTitleNameSuggest) {
        this.jobTitleNameSuggest = jobTitleNameSuggest;
    }
}
