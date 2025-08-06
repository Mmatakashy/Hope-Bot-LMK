package org.lmkenya.lmk_chatbot;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.dialogflow.v2.Dialogflow;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2Intent;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DialogflowClient {

    private static final String PROJECT_ID = "chatbot-jxes";
    private final Dialogflow dialogflow;

    public DialogflowClient(InputStream credentialsStream) throws IOException, GeneralSecurityException {
        this.dialogflow = buildDialogflowService(credentialsStream);
    }

    private Dialogflow buildDialogflowService(InputStream credentialsStream) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Dialogflow.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("Dialogflow API Client")
                .build();
    }


//    Returns the intent
    public List<GoogleCloudDialogflowV2Intent> getIntents() throws IOException, GeneralSecurityException {

        Dialogflow.Projects.Agent.Intents.List request = dialogflow.projects().agent().intents().list("projects/" + PROJECT_ID + "/agent");

        // Adding the intentView parameter to include full intent details
        request.setIntentView("INTENT_VIEW_FULL");

        List<GoogleCloudDialogflowV2Intent> intents = request.execute().getIntents();

        // Log the retrieved intents for debugging
        if (intents != null) {
            System.out.println("Retrieved intents count: " + intents.size());
            for (GoogleCloudDialogflowV2Intent intent : intents) {
                System.out.println("Intent: " + intent.getDisplayName());
            }
        } else {
            System.out.println("No intents retrieved.");
        }

        return intents;
    }

    // Find an intent by display name
    public GoogleCloudDialogflowV2Intent findIntentByDisplayName(String displayName) throws IOException {
        List<GoogleCloudDialogflowV2Intent> intents;
        try {
            intents = getIntents();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        if (intents != null) {
            for (GoogleCloudDialogflowV2Intent intent : intents) {
                if (intent.getDisplayName().equals(displayName)) {
                    return intent;
                }
            }
        }
        return null; // Intent not found
    }

    public void updateIntent(GoogleCloudDialogflowV2Intent intent) throws IOException {
        String intentName = intent.getName(); // Full intent name, e.g., "projects/project-id/agent/intents/intent-id"
        String[] intentNameParts = intentName.split("/");
        String intentId = intentNameParts[intentNameParts.length - 1]; // Extracting the intent-id
        String intentPath = "projects/" + PROJECT_ID + "/agent/intents/" + intentId;

        Dialogflow.Projects.Agent.Intents.Patch request = dialogflow.projects().agent().intents().patch(intentPath, intent);
        request.setUpdateMask("displayName,trainingPhrases,messages");
        request.execute();
    }

    public void deleteIntent(String intentId) throws IOException {
        // The intentId should be the full intent name, e.g., "projects/project-id/agent/intents/intent-id"
        dialogflow.projects().agent().intents().delete(intentId).execute();
    }

    public void createIntent(GoogleCloudDialogflowV2Intent intent) throws IOException {
        // Create the intent in Dialogflow
        Dialogflow.Projects.Agent.Intents.Create request = dialogflow.projects().agent().intents().create("projects/" + PROJECT_ID + "/agent", intent);
        request.execute();
    }



//    public GoogleCloudDialogflowV2DetectIntentResponse detectIntent(String text, String sessionId) throws IOException {
//        GoogleCloudDialogflowV2TextInput textInput = new GoogleCloudDialogflowV2TextInput().setText(text).setLanguageCode("en");
//        GoogleCloudDialogflowV2QueryInput queryInput = new GoogleCloudDialogflowV2QueryInput().setText(textInput);
//
//        Dialogflow.Projects.Agent.Sessions.DetectIntent detectIntent = dialogflow.projects().agent().sessions()
//                .detectIntent("projects/" + PROJECT_ID + "/agent/sessions/" + sessionId,
//                        new GoogleCloudDialogflowV2DetectIntentRequest().setQueryInput(queryInput));
//
//        return detectIntent.execute();
//    }
//
//    public void handleDialogflowResponse(GoogleCloudDialogflowV2QueryResult queryResult, HomeActivity homeActivity) {
//        String action = queryResult.getAction();
//        if ("input.unknown".equals(action)) {
//            // Handle no-match case
//            String fallbackResponse = queryResult.getFulfillmentText();
//            homeActivity.showFallbackResponse(fallbackResponse);
//        } else {
//            // Handle recognized intent case
//            // Extract and handle the recognized intent and its parameters
//        }
//    }
}
