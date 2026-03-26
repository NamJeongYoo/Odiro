package com.interstellar.odiro.place.mapper;

import com.interstellar.odiro.place.controller.dto.PlaceSearchResponse;
import com.interstellar.odiro.place.service.dto.MergedPlaceDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    List<PlaceSearchResponse> toPlaceSearchResponseList(List<MergedPlaceDTO> mergedPlaceDTOList);
}