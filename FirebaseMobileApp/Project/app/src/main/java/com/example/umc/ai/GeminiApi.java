package com.example.umc.ai;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiApi {

    // LƯU Ý QUAN TRỌNG: KHÔNG NÊN ĐỂ API KEY TRỰC TIẾP TRONG CODE NGUỒN.
    // HÃY SỬ DỤNG BIẾN MÔI TRƯỜNG HOẶC LƯU TRỮ AN TOÀN.
    private static final String API_KEY = "AIzaSyAzBHJsEA_tPM7nlD81r3-pXNNWGQR9pJs";

    // Đã cập nhật lên endpoint v1 và model gemini-2.5-flash
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public static void sendMessage(String prompt, GeminiCallback callback) {

        // ----------------------------------------------------------------------
        // Cấu hình OkHttpClient với thời gian chờ (Timeout) tăng lên 30 giây
        // để tránh lỗi java.net.SocketTimeoutException.
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 30s cho kết nối ban đầu
                .writeTimeout(30, TimeUnit.SECONDS)   // 30s cho việc gửi dữ liệu (request body)
                .readTimeout(30, TimeUnit.SECONDS)    // 30s cho việc nhận phản hồi từ server
                .build();
        // ----------------------------------------------------------------------

        JSONObject json = new JSONObject();
        try {
            json.put("contents",
                    new org.json.JSONArray()
                            .put(new JSONObject().put("parts",
                                    new org.json.JSONArray().put(
                                            new JSONObject().put("text", prompt)
                                    )
                            ))
            );
        } catch (Exception e) {
            callback.onError(e.toString());
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    // Trả về lỗi chi tiết hơn nếu request không thành công
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    callback.onError("HTTP " + response.code() + ": " + response.message() + "\nDetails: " + errorBody);
                    return;
                }

                String res = response.body().string();

                try {
                    JSONObject obj = new JSONObject(res);

                    // Kiểm tra xem có 'candidates' trước khi truy cập không
                    if (obj.has("candidates") && obj.getJSONArray("candidates").length() > 0) {
                        String reply = obj
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        callback.onSuccess(reply);
                    } else {
                        // Trường hợp không có 'candidates', thường là do lỗi Safety Block (vi phạm chính sách)
                        if (obj.has("promptFeedback")) {
                            callback.onError("API Blocked: Content rejected by safety filter or output is empty. Details: " + obj.getJSONObject("promptFeedback").toString());
                        } else {
                            callback.onError("Parse error: Candidates not found or empty.");
                        }
                    }

                } catch (Exception e) {
                    callback.onError("Parse error: " + e.toString() + "\nRaw response: " + res);
                }
            }
        });
    }
}