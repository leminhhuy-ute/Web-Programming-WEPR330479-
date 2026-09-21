package vn.edu.hcmute.topicmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.topicmanagement.dto.request.AnnouncementRequest;
import vn.edu.hcmute.topicmanagement.dto.response.AnnouncementResponse;
import vn.edu.hcmute.topicmanagement.entity.Announcement;
import vn.edu.hcmute.topicmanagement.entity.User;
import vn.edu.hcmute.topicmanagement.enums.AnnouncementAudience;
import vn.edu.hcmute.topicmanagement.exception.ResourceNotFoundException;
import vn.edu.hcmute.topicmanagement.repository.AnnouncementRepository;
import vn.edu.hcmute.topicmanagement.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    public List<AnnouncementResponse> findAll(String keyword, AnnouncementAudience audience) {
        return announcementRepository.search(keyword == null ? "" : keyword.trim(), audience)
                .stream()
                .map(this::map)
                .toList();
    }

    public AnnouncementResponse findById(Long id) {
        return map(getEntity(id));
    }

    public AnnouncementResponse create(AnnouncementRequest r, Long authorId) {
        Announcement a = new Announcement();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Người tạo không tồn tại."));
        a.setCreatedBy(author);
        return save(a, r);
    }

    public AnnouncementResponse update(Long id, AnnouncementRequest r) {
        return save(getEntity(id), r);
    }

    private AnnouncementResponse save(Announcement a, AnnouncementRequest r) {
        a.setTitle(r.title().trim());
        a.setContent(r.content().trim());
        a.setAudience(r.audience());
        return map(announcementRepository.save(a));
    }

    public void delete(Long id) {
        announcementRepository.delete(getEntity(id));
    }

    private Announcement getEntity(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
    }

    public AnnouncementResponse map(Announcement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getAudience().name(),
                a.getCreatedBy() == null ? null : a.getCreatedBy().getId(),
                a.getCreatedBy() == null ? null : a.getCreatedBy().getFullName(),
                a.getCreatedAt()
        );
    }
}
