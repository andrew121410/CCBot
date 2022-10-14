package com.andrew121410.ccbot.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AMessage {
    private String messageRawContent;
    private String authorId;
}
