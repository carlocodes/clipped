package com.carlocodes.clipped.dtos;

import java.util.Set;

public class UserDto {
    private long id;
    private String username;
    private Set<GameDto> games;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<GameDto> getGames() {
        return games;
    }

    public void setGames(Set<GameDto> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", games=" + games +
                '}';
    }
}
