package itsc4155.jobsearch.posting.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import itsc4155.jobsearch.util.StringUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import itsc4155.jobsearch.posting.Posting;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class FetchPostingsService {
    private final OkHttpClient client = new OkHttpClient();
    private ArrayList<Posting> cachedPostings = new ArrayList<>(); // Cache for job postings

    MediaType mediaType = MediaType.parse("application/json");

    @Scheduled(fixedRate = 3600000)  // 1 hour
    public void refreshJobListings() throws IOException {
        ArrayList<Posting> fetchedPostings = fetchJobsFromApi();
        if (fetchedPostings != null) {
            cachedPostings = fetchedPostings; // Update the cache
        }
        System.out.println("\n***REFRESHED API LIST***\n");
    }

    public ArrayList<Posting> getCachedPostings() {
        return cachedPostings; // Return cached postings
    }

    private ArrayList<Posting> fetchJobsFromApi() throws IOException {
        String body = """
                  {"page": 0,
                    "limit": 1,
                    "job_country_code_or": ["US"],
                    "posted_at_max_age_days": 7, 
                    "job_technology_slug_or": ["java", "c", "c#","c++", "python", "javascript"],
                    "job_description_pattern_or": ["frontend", "backend"],
                    "job_location_pattern_or": ["Charlotte", "Raleigh", "Atlanta"]}""" 
        ;
        Request request = new Request.Builder()
        .url("https://api.theirstack.com/v1/jobs/search")
        .post(RequestBody.create(body, mediaType))
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqYWRwZXRlMTIzQGdtYWlsLmNvbSIsInBlcm1pc3Npb25zIjoidXNlciIsImNyZWF0ZWRfYXQiOiIyMDI1LTAyLTI4VDIwOjQ1OjQ0LjI2NDY0NiswMDowMCJ9.GzntuYXkDYNglmsgmeBor0SaHX5EdD_ccw8ryXqreZs")
        .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            
            ArrayList<Posting> list = new ArrayList<Posting>();
            
            String json = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);

            JsonNode dataArray = rootNode.path("data");
            System.out.println("RESPONSE \nSUCCESSFULL");

            if (dataArray.isArray()) {
                for (JsonNode jobNode : dataArray) {
                    String jobTitle = jobNode.path("job_title").asText();
                    String companyName = jobNode.path("company").asText(); 
                    String description = jobNode.path("description").asText();
                    String timeStamp = jobNode.path("date_posted").asText();
                    String url = jobNode.path("url").asText();

                    Posting posting = new Posting();
                    posting.setId("thirdparty-" + list.size() + 1);
                    posting.setJobTitle(jobTitle);
                    posting.setCompanyName(companyName);
                    posting.setJobDescription(StringUtil.trimToFirstWords(description, 40) + "...");
                    posting.setAuthorUsername(companyName);
                    posting.setUrl(url);
                    posting.setPulledViaApi(true);

                    LocalDate date = LocalDate.parse(timeStamp);
                    long epochMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

                    posting.setTimestamp(epochMillis);

                    list.add(posting);
                    System.out.println(list.get(0));
                }
            }
            System.out.println("returned: " + list);
            return list;
        } catch(Exception e){
            System.out.println("ERROR \nFETCHING \nPOSTINGS");
            System.out.println(e);
            return null;
            
        }
    }

    public Posting findById(String id) {
        return cachedPostings.stream().filter(posting -> posting.getId().equals(id)).findFirst().orElse(null);
    }
}