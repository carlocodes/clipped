package com.carlocodes.clipped.mappers;

import com.carlocodes.clipped.dtos.GameDto;
import com.carlocodes.clipped.entities.Game;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameDto mapToDto(Game game);

    List<GameDto> mapToDtos(List<Game> games);

    Set<GameDto> mapToDtos(Set<Game> games);
}
