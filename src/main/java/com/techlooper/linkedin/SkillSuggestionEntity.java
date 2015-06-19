package com.techlooper.linkedin;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Document(indexName = "suggester", type = "linkedInSkill")
public class SkillSuggestionEntity {

    @Id
    private String skill;

    @CompletionField(payloads = true, maxInputLength = 50, indexAnalyzer = "index_analyzer", searchAnalyzer = "search_analyzer")
    private Completion skillSuggest;

    public SkillSuggestionEntity(String skill) {
        this.skill = skill;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Completion getSkillSuggest() {
        return skillSuggest;
    }

    public void setSkillSuggest(Completion skillSuggest) {
        this.skillSuggest = skillSuggest;
    }
}
