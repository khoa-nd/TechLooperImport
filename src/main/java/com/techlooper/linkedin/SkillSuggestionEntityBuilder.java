package com.techlooper.linkedin;

import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
public class SkillSuggestionEntityBuilder {

    private SkillSuggestionEntity result;

    public SkillSuggestionEntityBuilder(String skill) {
        result = new SkillSuggestionEntity(skill);
    }

    public SkillSuggestionEntityBuilder suggest(String[] input, String output) {
        Completion skillSuggest = new Completion(input);
        skillSuggest.setOutput(output);

        result.setSkillSuggest(skillSuggest);
        return this;
    }

    public SkillSuggestionEntity build() {
        return result;
    }

    public IndexQuery buildIndex() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId(result.getSkill());
        indexQuery.setObject(result);
        return indexQuery;
    }
}
