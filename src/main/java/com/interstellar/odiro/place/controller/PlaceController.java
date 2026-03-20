package com.interstellar.odiro.place.controller;

import com.interstellar.odiro.place.service.PlaceSearchService;
import com.interstellar.odiro.place.service.dto.MergedPlaceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/place")
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    @GetMapping
    public List<MergedPlaceDTO> getPlaces(@RequestParam String keyword) {
        return placeSearchService.searchPlacesParallel(keyword);
    }
}