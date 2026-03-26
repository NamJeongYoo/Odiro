package com.interstellar.odiro.place.controller.dto;

public record PlaceSearchResponse (
        String title,
        String address,
        String roadAddress,
        String phone,
        String category,
        String mapX,
        String mapY,
        String link,
        String apiSource
){

}
