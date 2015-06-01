package com.techlooper.suggestion;

import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
public class JobTitleSuggestionEntityBuilder {

    private JobTitleSuggestionEntity result;

    public JobTitleSuggestionEntityBuilder(String jobTitleName) {
        result = new JobTitleSuggestionEntity(jobTitleName);
    }

    public JobTitleSuggestionEntityBuilder suggest(String[] input, String output) {
        Completion jobTitleSuggest = new Completion(input);
        jobTitleSuggest.setOutput(output);

        result.setJobTitleNameSuggest(jobTitleSuggest);
        return this;
    }

    public JobTitleSuggestionEntity build() {
        return result;
    }

    public IndexQuery buildIndex() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId(result.getJobTitleName());
        indexQuery.setObject(result);
        return indexQuery;
    }
}
