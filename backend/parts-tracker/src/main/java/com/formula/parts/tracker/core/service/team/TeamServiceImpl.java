package com.formula.parts.tracker.core.service.team;

import com.formula.parts.tracker.core.mapper.TeamMapper;
import com.formula.parts.tracker.core.mapper.TransportCompanyMapper;
import com.formula.parts.tracker.core.service.transportcompany.TransportCompanyService;
import com.formula.parts.tracker.dao.model.Address;
import com.formula.parts.tracker.dao.model.Storage;
import com.formula.parts.tracker.dao.model.Team;
import com.formula.parts.tracker.dao.model.TransportCompany;
import com.formula.parts.tracker.dao.repository.TeamMemberRepository;
import com.formula.parts.tracker.dao.repository.TeamRepository;
import com.formula.parts.tracker.dao.repository.TransportCompanyRepository;
import com.formula.parts.tracker.shared.dto.Page;
import com.formula.parts.tracker.shared.dto.storage.StorageResponse;
import com.formula.parts.tracker.shared.dto.team.TeamMemberResponse;
import com.formula.parts.tracker.shared.dto.team.TeamRequest;
import com.formula.parts.tracker.shared.dto.team.TeamResponse;
import com.formula.parts.tracker.shared.dto.transportcompany.TransportCompanyRequest;
import com.formula.parts.tracker.shared.dto.transportcompany.TransportCompanyResponse;
import com.formula.parts.tracker.shared.exception.ApiException;
import com.formula.parts.tracker.shared.exception.BadRequestException;
import com.formula.parts.tracker.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMapper teamMapper;

    private static final String NAME = "name";

    @Override
    public TeamResponse create(final TeamRequest request) throws ApiException {
        validateUniqueFieldConstraint(teamRepository::existsByName,
                request.getName(), NAME);

        Team team = teamMapper.toEntity(request);
        team = teamRepository.persist(team);

        return teamMapper.toResponse(team);
    }

    @Override
    public Page<TeamResponse> getAllTeams(String search, Long page, Long size) {
        List<Team> teams;
        Long totalElements;

        if (search != null && !search.trim().isEmpty()) {
            teams = teamRepository.findByNameLike(search, page, size);
            totalElements = teamRepository.countByNameLike(search);
        } else {
            teams = teamRepository.findAll(page, size);
            totalElements = teamRepository.countAll();
        }

        Long totalPages = (totalElements + size - 1) / size;

        Page<TeamResponse> pageResponse = new Page<>();
        pageResponse.setContent(teams.stream()
                .map(teamMapper::toResponse)
                .toList());
        pageResponse.setPageNumber(page);
        pageResponse.setPageSize(size);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages(totalPages);

        return pageResponse;
    }

    @Override
    public TeamResponse getTeam(long id) throws ApiException {
        if (!teamRepository.existsById(id)) {
            throw new NotFoundException(String.format("Team with ID %d does not exist.", id));
        }

        Team team = teamRepository.findById(id);
        TeamResponse response = teamMapper.toResponse(team);
        response.setTeamMembers(teamRepository.findTeamMembersByTeamId(id));
        return response;
    }

    @Override
    public void assignMember(Long teamId, Long userId) throws ApiException {
        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException("Team not found");
        }
        if (teamMemberRepository.exists(teamId, userId)) {
            throw new BadRequestException("User is already a member of this team.");
        }

        teamMemberRepository.insert(teamId, userId);
    }

    @Override
    public void removeTeamMember(Long teamId, Long userId) throws ApiException {
        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException("Team not found");
        }
        if (!teamMemberRepository.exists(teamId, userId)) {
            throw new NotFoundException("User is not a member of this team.");
        }

        teamMemberRepository.delete(teamId, userId);
    }

    @Override
    public List<TeamMemberResponse> getAvailableLogisticUsers() {
        return teamRepository.findAvailableLogisticUsers();
    }

    @Override
    public List<TeamMemberResponse> getAvailableMechanicUsers() {
        return teamRepository.findAvailableMechanicUsers();
    }

    private <T> void validateUniqueFieldConstraint(final Predicate<T> existsByField,
                                                   final T fieldValue, final String fieldName) throws BadRequestException {
        if (Boolean.TRUE.equals(existsByField.test(fieldValue))) {
            throw new BadRequestException(
                    String.format("Provided %s is already taken.", fieldName));
        }
    }
}
