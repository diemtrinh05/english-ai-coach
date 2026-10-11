package com.example.englishaicoach.data.dto;

public final class AuthFormDtos {
    private AuthFormDtos() { }
    public static final class Login {
        public final String email, password;
        public Login(String email, String password) { this.email = email; this.password = password; }
    }
    public static final class Register {
        public final String fullName, email, password;
        public Register(String fullName, String email, String password) {
            this.fullName = fullName; this.email = email; this.password = password;
        }
    }
    @com.google.gson.annotations.JsonAdapter(ResponseReader.class)
    public static final class Response {
        public String accessToken, refreshToken, tokenType;
        public Long expiresIn;
        public User user;
        public boolean valid() {
            return accessToken != null && !accessToken.trim().isEmpty()
                    && refreshToken != null && !refreshToken.trim().isEmpty()
                    && "Bearer".equals(tokenType) && expiresIn != null && expiresIn > 0
                    && user != null && user.id != null && !user.id.isEmpty();
        }
    }
    public static final class User { public String id, email, fullName, role; }
    public static final class ResponseReader implements com.google.gson.JsonDeserializer<Response> {
        @Override public Response deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type type,
                com.google.gson.JsonDeserializationContext context) {
            if (!json.isJsonObject()) throw new com.google.gson.JsonParseException("Invalid auth response");
            com.google.gson.JsonObject object = json.getAsJsonObject();
            Response result = new Response();
            result.accessToken = string(object, "accessToken"); result.refreshToken = string(object, "refreshToken");
            result.tokenType = string(object, "tokenType");
            com.google.gson.JsonElement expiry = object.get("expiresIn");
            if (expiry == null || !expiry.isJsonPrimitive() || !expiry.getAsJsonPrimitive().isNumber())
                throw new com.google.gson.JsonParseException("Invalid expiry");
            try { result.expiresIn = expiry.getAsBigDecimal().longValueExact(); }
            catch (ArithmeticException invalid) { throw new com.google.gson.JsonParseException("Invalid expiry"); }
            com.google.gson.JsonElement user = object.get("user");
            if (user == null || !user.isJsonObject()) throw new com.google.gson.JsonParseException("Invalid user");
            result.user = new User(); result.user.id = string(user.getAsJsonObject(), "id");
            result.user.email = string(user.getAsJsonObject(), "email");
            result.user.fullName = string(user.getAsJsonObject(), "fullName");
            result.user.role = string(user.getAsJsonObject(), "role");
            return result;
        }
        private static String string(com.google.gson.JsonObject object, String field) {
            com.google.gson.JsonElement value = object.get(field);
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
                throw new com.google.gson.JsonParseException("Invalid auth field");
            return value.getAsString();
        }
    }
}
