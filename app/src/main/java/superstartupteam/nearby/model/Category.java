package superstartupteam.nearby.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by kerrk on 8/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category extends BaseEntity {
    private String name;

    private List<String> examples;

    public Category() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }
}