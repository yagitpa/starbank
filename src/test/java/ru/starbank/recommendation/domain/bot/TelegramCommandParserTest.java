package ru.starbank.recommendation.domain.bot;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramCommandParserTest {

    @Test
    void parse_shouldParseStart() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        Optional<TelegramCommand> cmd = parser.parse("/start");

        assertTrue(cmd.isPresent());
        assertEquals("start", cmd.get().name());
        assertNull(cmd.get().argument());
    }

    @Test
    void parse_shouldParseRecommendWithUsername() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        Optional<TelegramCommand> cmd = parser.parse("/recommend user123");

        assertTrue(cmd.isPresent());
        assertEquals("recommend", cmd.get().name());
        assertEquals("user123", cmd.get().argument());
    }

    @Test
    void parse_shouldHandleMultipleSpaces() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        Optional<TelegramCommand> cmd = parser.parse("   /recommend     user123    ");

        assertTrue(cmd.isPresent());
        assertEquals("recommend", cmd.get().name());
        assertEquals("user123", cmd.get().argument());
    }

    @Test
    void parse_shouldParseRecommendWithBotMention() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        Optional<TelegramCommand> cmd = parser.parse("/recommend@Recommendations100500Bot user123");

        assertTrue(cmd.isPresent());
        assertEquals("recommend", cmd.get().name());
        assertEquals("user123", cmd.get().argument());
    }

    @Test
    void parse_shouldIgnoreCommandForAnotherBot() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        Optional<TelegramCommand> cmd = parser.parse("/recommend@AnotherBot user123");

        assertTrue(cmd.isEmpty());
    }

    @Test
    void parse_shouldReturnEmptyForNonCommandText() {
        TelegramCommandParser parser = new TelegramCommandParser("Recommendations100500Bot");

        assertTrue(parser.parse("hello").isEmpty());
        assertTrue(parser.parse("  hello  ").isEmpty());
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
    }
}