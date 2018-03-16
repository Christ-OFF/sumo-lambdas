package com.christoff.apps.sumolambda.utils;

import com.christoff.apps.sumolambda.RikishiPictureScrapperService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PictureOptimizerTest {

    @NotNull
    static Stream<Arguments> twoQualitiesProvider() {
        return Stream.of(
            Arguments.of(0.1F, 0.2F),
            Arguments.of(0.2F, 0.3F),
            Arguments.of(0.4F, 0.5F),
            Arguments.of(0.5F, 0.6F),
            Arguments.of(0.6F, 0.7F),
            Arguments.of(0.8F, 0.9F),
            Arguments.of(0.9F, 1.0F)
        );
    }

    @ParameterizedTest(name = "[{index}] image between {0} and {1} quality")
    @MethodSource("twoQualitiesProvider")
    void should_reduce_size_according_to_quality(Float lowerQuality, Float higherQuality) throws IOException {
        byte[] picture = RikishiPictureScrapperService.getDefaultRikishiPicture();
        byte[] resultLower = PictureOptimizer.reducePicture(picture, lowerQuality);
        byte[] resultHigher = PictureOptimizer.reducePicture(picture, higherQuality);
        assertTrue(resultLower.length < resultHigher.length);
    }

    @Test
    void should_reduce_size_but_nor_more_than_source() throws IOException {
        byte[] picture = RikishiPictureScrapperService.getDefaultRikishiPicture();
        byte[] result8 = PictureOptimizer.reducePicture(picture, 0.8F);
        byte[] result9 = PictureOptimizer.reducePicture(picture, 0.9F);
        assertTrue(result8.length < picture.length);
        assertTrue(result9.length > picture.length);
    }

}
