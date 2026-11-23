package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.json.JsonFactory;

/**
 * Factory for clients. It creates a Client from the JSON written by
 * Client.toJson(). Gson reads the values, then the constructor is called, so
 * every rule is checked again.
 */
public final class ClientFactory {

    private ClientFactory() {
    }

    private record Data(int id, String name, String email, String phone) {
    }

    public static Client fromJson(JsonObject o) {
        Data data = JsonFactory.create().fromJson(o, Data.class);
        return new Client(data.id(), data.name(), data.email(), data.phone());
    }
}
