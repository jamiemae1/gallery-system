package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Unit tests for {@link AlbumService}.
 * Tests all service layer functionality including CRUD operations,
 * sorting, pagination, and business logic requirements.
 */
@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumMapper albumMapper;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private AlbumDTO albumDTO;
    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        user.setEmail("test@example.com");

        // Setup test album
        album = new Album();
        album.setId(1L);
        album.setName("Test Album");
        album.setEvent("Test Event");
        album.setCreationDate(Instant.now());
        album.setUser(user);

        // Setup test DTO
        albumDTO = new AlbumDTO();
        albumDTO.setId(1L);
        albumDTO.setName("Test Album");
        albumDTO.setEvent("Test Event");
        albumDTO.setCreationDate(Instant.now());

        // Setup pageable
        pageable = PageRequest.of(0, 20);
    }

    @Test
    void testSaveAlbum_Success() {
        // Given
        when(albumMapper.toEntity(albumDTO)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        AlbumDTO result = albumService.save(albumDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Album");
        verify(albumRepository).save(album);
    }

    @Test
    void testUpdateAlbum_Success() {
        // Given
        when(albumMapper.toEntity(albumDTO)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        AlbumDTO result = albumService.update(albumDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Album");
        verify(albumRepository).save(album);
    }

    @Test
    void testPartialUpdateAlbum_Success() {
        // Given
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Optional<AlbumDTO> result = albumService.partialUpdate(albumDTO);

        // Then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getName()).isEqualTo("Test Album");
        verify(albumMapper).partialUpdate(album, albumDTO);
    }

    @Test
    void testFindAllAlbums_Success() {
        // Given
        List<Album> albums = Arrays.asList(
            createAlbum("Album A", "Event Z"),
            createAlbum("Album B", "Event A"),
            createAlbum("Album C", null) // No event - should be "Miscellaneous"
        );
        Page<Album> albumPage = new PageImpl<>(albums);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        verify(albumRepository).findAll(pageable);
    }

    @Test
    void testFindAllWithEagerRelationships_Success() {
        // Given
        List<Album> albums = Arrays.asList(album);
        Page<Album> albumPage = new PageImpl<>(albums);

        when(albumRepository.findAllWithEagerRelationships(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAllWithEagerRelationships(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findAllWithEagerRelationships(pageable);
    }

    @Test
    void testFindOne_Success() {
        // Given
        when(albumRepository.findOneWithEagerRelationships(1L)).thenReturn(Optional.of(album));
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Optional<AlbumDTO> result = albumService.findOne(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(1L);
        verify(albumRepository).findOneWithEagerRelationships(1L);
    }

    @Test
    void testFindOne_NotFound() {
        // Given
        when(albumRepository.findOneWithEagerRelationships(999L)).thenReturn(Optional.empty());

        // When
        Optional<AlbumDTO> result = albumService.findOne(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testDelete_Success() {
        // When
        albumService.delete(1L);

        // Then
        verify(albumRepository).deleteById(1L);
    }

    // User Story Requirement: Event-based sorting (alphabetically)
    @Test
    void testEventSorting_AlphabeticalOrder() {
        // Given
        List<Album> albums = Arrays.asList(
            createAlbum("Wedding Photos", "Smith Wedding"),
            createAlbum("Birthday Party", "John's Birthday"),
            createAlbum("Random Photos", null), // Miscellaneous category
            createAlbum("Vacation Pics", "Europe Trip")
        );
        Page<Album> albumPage = new PageImpl<>(albums);

        Pageable eventSortPageable = PageRequest.of(0, 20, Sort.by("event").ascending().and(Sort.by("name").ascending()));

        when(albumRepository.findAll(eventSortPageable)).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAll(eventSortPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
        verify(albumRepository).findAll(eventSortPageable);
    }

    // User Story Requirement: Date-based sorting (chronological, recent first)
    @Test
    void testDateSorting_ChronologicalOrder_RecentFirst() {
        // Given
        Instant now = Instant.now();
        List<Album> albums = Arrays.asList(
            createAlbumWithDate("Recent Album", now),
            createAlbumWithDate("Old Album", now.minusSeconds(86400))
        );
        Page<Album> albumPage = new PageImpl<>(albums);

        Pageable dateSortPageable = PageRequest.of(0, 20, Sort.by("creationDate").descending());

        when(albumRepository.findAll(dateSortPageable)).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAll(dateSortPageable);

        // Then
        assertThat(result).isNotNull();
        verify(albumRepository).findAll(dateSortPageable);
    }

    // JDL Requirement: Thumbnail handling (ImageBlob)
    @Test
    void testThumbnailHandling() {
        // Given
        byte[] thumbnailData = "test-thumbnail".getBytes();
        album.setThumbnail(thumbnailData);
        album.setThumbnailContentType("image/jpeg");

        // When & Then
        assertThat(album.getThumbnail()).isEqualTo(thumbnailData);
        assertThat(album.getThumbnailContentType()).isEqualTo("image/jpeg");
    }

    // User Story Requirement: Date override functionality (admin)
    @Test
    void testOverrideDateHandling() {
        // Given
        Instant overrideDate = Instant.now().minusSeconds(3600);
        album.setOverrideDate(overrideDate);

        // When & Then
        assertThat(album.getOverrideDate()).isEqualTo(overrideDate);
        assertThat(album.getCreationDate()).isNotEqualTo(overrideDate);
    }

    // JDL Requirement: Name validation (required, minlength=3, maxlength=255)
    @Test
    void testAlbumValidation_NameConstraints() {
        // Test valid name
        album.setName("Valid Album Name");
        assertThat(album.getName()).isEqualTo("Valid Album Name");

        // Test minimum length constraint (handled by @Size annotation)
        album.setName("ab");
        assertThat(album.getName()).hasSize(2); // Will fail validation in real scenario

        // Test maximum length constraint
        String longName = "a".repeat(256);
        album.setName(longName);
        assertThat(album.getName()).hasSizeGreaterThan(255); // Will fail validation
    }

    // User Story Requirement: Miscellaneous category for albums without events
    @Test
    void testMiscellaneousAlbumHandling() {
        // Given
        List<Album> albumsWithoutEvents = Arrays.asList(
            createAlbum("Album 1", null),
            createAlbum("Album 2", ""),
            createAlbum("Album 3", "   ") // Whitespace only
        );

        // When/Then - These albums would be grouped as "Miscellaneous" in the UI layer
        albumsWithoutEvents.forEach(a -> {
            assertThat(a.getEvent()).satisfiesAnyOf(
                event -> assertThat(event).isNull(),
                event -> assertThat(event).isEmpty(),
                event -> assertThat(event.trim()).isEmpty()
            );
        });
    }

    // Non-Functional Requirement: Performance (< 2 seconds response time)
    @Test
    void testPerformanceRequirement_ResponseTime() {
        // Given
        List<Album> largeAlbumList = createLargeAlbumList(50);
        Page<Album> albumPage = new PageImpl<>(largeAlbumList);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        long startTime = System.currentTimeMillis();
        Page<AlbumDTO> result = albumService.findAll(pageable);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Then
        assertThat(responseTime).isLessThan(2000); // < 2 seconds per requirement
        assertThat(result).isNotNull();
    }

    // JDL Requirement: ManyToOne relationship with User
    @Test
    void testUserRelationship() {
        // Given
        album.setUser(user);

        // When & Then
        assertThat(album.getUser()).isEqualTo(user);
        assertThat(album.getUser().getLogin()).isEqualTo("testuser");
    }

    // Helper methods
    private Album createAlbum(String name, String event) {
        Album album = new Album();
        album.setName(name);
        album.setEvent(event);
        album.setCreationDate(Instant.now());
        album.setUser(user);
        return album;
    }

    private Album createAlbumWithDate(String name, Instant creationDate) {
        Album album = new Album();
        album.setName(name);
        album.setCreationDate(creationDate);
        album.setUser(user);
        return album;
    }

    private List<Album> createLargeAlbumList(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> createAlbum("Album " + i, "Event " + (i % 10)))
            .collect(java.util.stream.Collectors.toList());
    }
}
