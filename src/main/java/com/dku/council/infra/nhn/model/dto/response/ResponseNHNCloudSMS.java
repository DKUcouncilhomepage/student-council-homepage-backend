package com.dku.council.infra.nhn.model.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseNHNCloudSMS {
    private Header header;
    private Body body;

    @Data
    public static class Header {
        private boolean isSuccessful;
        private int resultCode;
        private String resultMessage;

        // isSuccessful은 @Getter tag로 인해 isSuccessful() getter가 만들어진다.
        // 그래서 파싱할 때 제대로 파싱이 안된다.
        // 따로 getIsSuccessful을 만들어주어야 함. (setter도 마찬가지)
        public boolean getIsSuccessful() {
            return isSuccessful;
        }

        public void setIsSuccessful(boolean successful) {
            isSuccessful = successful;
        }
    }

    @Data
    public static class Body {
        private Data data;

        @lombok.Data
        public static class Data {
            private String requestId;
            private String statusCode;
            private List<SendResult> sendResultList;

            @lombok.Data
            public static class SendResult {
                private String recipientNo;
                private String resultCode;
                private String resultMessage;
            }
        }
    }
}
