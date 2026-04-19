    package com.example.smartkarobar;

    import android.util.Log;

    import org.json.JSONArray;
    import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.io.OutputStream;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    public class GeminiHelper {

        private static final String TAG = "GeminiHelper";
        private static final String API_KEY ="AIzaSyDFqecDeuCmgwYKhuQU2S8N7_rEkdfOA2I";
//        private static final String MODEL = "gemini-2.0-flash";
        private static final String MODEL = "gemini-3-flash-preview";
        private static final String URL_STRING = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + API_KEY;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        public interface GeminiCallback {
            void onResponse(String advice);
            void onError(Throwable t);
        }

        private void getAdviceWithPrompt(String prompt, GeminiCallback callback) {
            executor.execute(() -> {
                try {
                    Log.d(TAG, "Calling REST API with model: " + MODEL);

                    JSONObject requestBody = new JSONObject();
                    JSONArray contents = new JSONArray();
                    JSONObject content = new JSONObject();
                    JSONArray parts = new JSONArray();
                    JSONObject part = new JSONObject();

                    part.put("text", prompt);
                    parts.put(part);
                    content.put("parts", parts);
                    contents.put(content);
                    requestBody.put("contents", contents);

                    HttpURLConnection conn = (HttpURLConnection) new URL(URL_STRING).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(requestBody.toString().getBytes("UTF-8"));
                    }

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response code: " + responseCode);

                    BufferedReader reader;
                    if (responseCode == 200) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    String rawResponse = sb.toString();
                    Log.d(TAG, "Raw response: " + rawResponse);

                    if (responseCode == 200) {
                        JSONObject json = new JSONObject(rawResponse);
                        String text = json
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                        Log.d(TAG, "Parsed response: " + text);
                        callback.onResponse(text);
                    } else {
                        Log.e(TAG, "API error body: " + rawResponse);
                        callback.onError(new Exception("HTTP " + responseCode + ": " + rawResponse));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Request failed", e);
                    callback.onError(e);
                }
            });
        }

        public void getBusinessAdvice(double sales, double expenses, double receivables, double payables, GeminiCallback callback) {
            Log.d(TAG, "getBusinessAdvice — sales=" + sales + " expenses=" + expenses);
            String prompt = "You are a professional business consultant for 'SmartKarobar', a POS app for small shopkeepers in Pakistan. " +
                    "Based on this month's data:\n" +
                    "- Total Sales: Rs. " + (int) sales + "\n" +
                    "- Total Expenses: Rs. " + (int) expenses + "\n" +
                    "- Total Udhaar (Receivables): Rs. " + (int) receivables + "\n" +
                    "- Total Payables: Rs. " + (int) payables + "\n\n" +
                    "Provide a 2-sentence business tip in Roman Urdu (Urdu written in English script) that is encouraging and practical.";
            getAdviceWithPrompt(prompt, callback);
        }

        public void askBusinessQuestion(
                double sales, double expenses, double receivables, double payables,
                String question, List<Map<String, String>> chatHistory,
                GeminiCallback callback) {

            Log.d(TAG, "askBusinessQuestion — question: " + question);

            StringBuilder history = new StringBuilder();
            for (Map<String, String> msg : chatHistory) {
                history.append(msg.get("role")).append(": ").append(msg.get("text")).append("\n");
            }

            String prompt = "You are a friendly Urdu-English (Hinglish) business analyst for a Pakistani small business app called SmartKarobar.\n" +
                    "Business data: Sales=Rs." + (int) sales + ", Expenses=Rs." + (int) expenses +
                    ", Receivables=Rs." + (int) receivables + ", Payables=Rs." + (int) payables + "\n" +
                    "Chat history:\n" + history +
                    "User asks: " + question + "\n" +
                    "Reply in Hinglish (mix of Urdu words in Roman script and English). Keep it under 3 sentences. Be specific with numbers.";

            getAdviceWithPrompt(prompt, callback);
        }
    }