package life.genny;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Before;
import org.junit.Test;

import life.genny.notes.models.Note;

public class LocalDateTimeTest {
    
    private Jsonb jsonb;
    
    @Before
    public void init() {
        this.jsonb = JsonbBuilder.
        newBuilder().
        build();
    }

//    @Test
//    public void serialize() {
//        LocalDateTime developer =LocalDateTime.now();
//        String serialized = this.jsonb.toJson(developer);
//        System.out.println("serialized = " + serialized);
//       
//
//    }

//    @Test
//    public void deserialize() {
//        String deserialzed = " {\n" + 
//        		"  \"id\": 0,\n" + 
//        		"  \"content\": \"string\",\n" + 
//        		"  \"created\": \"2020-06-22T11:33:223Z\",\n" + 
//        		"  \"sourceCode\": \"string\",\n" + 
//        		"  \"tags\": [\n" + 
//        		"    {\n" + 
//        		"      \"name\": \"string\",\n" + 
//        		"      \"value\": 0\n" + 
//        		"    }\n" + 
//        		"  ],\n" + 
//        		"  \"targetCode\": \"string\",\n" + 
//        		"  \"updated\": \"2020-06-22T11:33:223Z\"\n" + 
//        		"}";
//        Note duke = this.jsonb.fromJson(deserialzed, Note.class);
//       // assertThat(duke.created.getYear(), is(1995));
//    }

}