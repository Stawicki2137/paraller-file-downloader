package pl.mateusz.downloader.service;

import org.junit.jupiter.api.Test;
import pl.mateusz.downloader.domain.ByteRange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FixedSizeChunkPlannerTest {

    private final FixedSizeChunkPlanner planner = new FixedSizeChunkPlanner();

    @Test
    void shouldReturnEmptyListForEmptyFile() {
        List<ByteRange> ranges = planner.planChunks(0, 4);

        assertTrue(ranges.isEmpty());
    }

    @Test
    void shouldCreateSingleChunkWhenFileIsSmallerThanChunkSize() {
        List<ByteRange> ranges = planner.planChunks(3, 10);

        assertEquals(1, ranges.size());
        assertEquals(new ByteRange(0, 2), ranges.get(0));
    }

    @Test
    void shouldCreateMultipleChunksForExactDivision() {
        List<ByteRange> ranges = planner.planChunks(8, 4);

        assertEquals(List.of(
                new ByteRange(0, 3),
                new ByteRange(4, 7)
        ), ranges);
    }

    @Test
    void shouldCreateSmallerLastChunkWhenFileSizeIsNotDivisible() {
        List<ByteRange> ranges = planner.planChunks(10, 4);

        assertEquals(List.of(
                new ByteRange(0, 3),
                new ByteRange(4, 7),
                new ByteRange(8, 9)
        ), ranges);
    }

    @Test
    void shouldThrowWhenContentLengthIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> planner.planChunks(-1, 4));
    }

    @Test
    void shouldThrowWhenChunkSizeIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> planner.planChunks(10, 0));
        assertThrows(IllegalArgumentException.class, () -> planner.planChunks(10, -1));
    }
}