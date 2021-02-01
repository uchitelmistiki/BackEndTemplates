package templates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import megafon.atm.dataset.DataSet;
import megafon.atm.dataset.pom.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JSONConverter {
    public String convertDataSetToJson(String name, List<? extends DataSet> objects) {
        Gson gson = new GsonBuilder().create();
        JsonArray jarray = gson.toJsonTree(objects).getAsJsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(name, jarray);
        byte[] bytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
        return utf8EncodedString;
    }

    public Object convertJsonObjectToDataSet(String json, TypeToken type){
        Gson gson = new GsonBuilder().create();
        Type dataSetType = type.getType();
        Object dataSet = gson.fromJson(json, dataSetType);
        return dataSet;
    }

    public ArrayList<? extends DataSet> convertJsonArrayToDataSetList(String json, TypeToken type, ArrayList<? extends DataSet> data){
        Gson gson = new GsonBuilder().create();
        Type dataSetListType = type.getType();
        data = gson.fromJson(json, dataSetListType);
        return data;
    }

    public String getFieldValueFomJsonObject(JsonObject jsonObject, String field){
        String value = "";
        value = jsonObject.get(field).getAsString();
        return value;
    }
}
