package com.ObjDetec.nhandienvatthe;

import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;

import java.util.UUID;
import android.os.AsyncTask;

public class ChatbotHelper {

    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private static final String PROJECT_ID = "";

    public ChatbotHelper() {
        try {
            sessionsClient = SessionsClient.create();
            sessionName = SessionName.of(PROJECT_ID, UUID.randomUUID().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, ResponseListener listener) {
        QueryInput queryInput = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();

        new RequestTask(sessionsClient, sessionName, queryInput, listener).execute();
    }

    public interface ResponseListener {
        void onResponse(String response);
        void onError(Exception e);
    }

    private static class RequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {
        private final SessionsClient sessionsClient;
        private final SessionName sessionName;
        private final QueryInput queryInput;
        private final ResponseListener listener;

        RequestTask(SessionsClient sessionsClient, SessionName sessionName, QueryInput queryInput, ResponseListener listener) {
            this.sessionsClient = sessionsClient;
            this.sessionName = sessionName;
            this.queryInput = queryInput;
            this.listener = listener;
        }

        @Override
        protected DetectIntentResponse doInBackground(Void... voids) {
            try {
                return sessionsClient.detectIntent(sessionName, queryInput);
            } catch (Exception e) {
                listener.onError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(DetectIntentResponse response) {
            if (response != null) {
                listener.onResponse(response.getQueryResult().getFulfillmentText());
            } else {
                listener.onError(new Exception("No response from server"));
            }
        }
    }
}
