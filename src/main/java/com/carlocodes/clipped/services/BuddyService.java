package com.carlocodes.clipped.services;

import com.carlocodes.clipped.dtos.BuddyDto;
import com.carlocodes.clipped.dtos.BuddyRequestDto;
import com.carlocodes.clipped.entities.Buddy;
import com.carlocodes.clipped.entities.User;
import com.carlocodes.clipped.exceptions.ClippedException;
import com.carlocodes.clipped.mappers.BuddyMapper;
import com.carlocodes.clipped.repositories.BuddyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class BuddyService {
    private final BuddyRepository buddyRepository;
    private final UserService userService;

    public BuddyService(BuddyRepository buddyRepository,
                        UserService userService) {
        this.buddyRepository = buddyRepository;
        this.userService = userService;
    }

    public void sendBuddyRequest(BuddyRequestDto buddyRequestDto) throws ClippedException {
        try {
            long senderId = buddyRequestDto.getSenderId();
            long receiverId = buddyRequestDto.getReceiverId();

            if (senderId == receiverId)
                throw new ClippedException(String.format("User with id: %d cannot send buddy request to themselves!", senderId));

            User sender = userService.findById(senderId)
                    .orElseThrow(() -> new ClippedException(String.format("Sender with id: %d does not exist!", senderId)));
            User receiver = userService.findById(receiverId)
                    .orElseThrow(() -> new ClippedException(String.format("Receiver with id: %d does not exist!", receiverId)));

            if (buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(sender, receiver) ||
                    buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(receiver, sender))
                throw new ClippedException("Users are already buddies!");

            if (buddyRepository.existsBySenderAndReceiverAndAcceptedIsNull(sender, receiver) ||
                    buddyRepository.existsBySenderAndReceiverAndAcceptedIsNull(receiver, sender))
                throw new ClippedException("Buddy request has already been sent!");

            saveBuddyRequest(sender, receiver);
        } catch (ClippedException e) {
            throw new ClippedException(String.format("Send buddy request from sender with id: %d to receiver with id: %d failed due to %s",
                    buddyRequestDto.getSenderId(), buddyRequestDto.getReceiverId(), e.getMessage()), e);
        }
    }

    public void acceptBuddyRequest(BuddyRequestDto buddyRequestDto) throws ClippedException {
        try {
            long senderId = buddyRequestDto.getSenderId();
            long receiverId = buddyRequestDto.getReceiverId();

            User sender = userService.findById(senderId)
                    .orElseThrow(() -> new ClippedException(String.format("Sender with id: %d does not exist!", senderId)));
            User receiver = userService.findById(receiverId)
                    .orElseThrow(() -> new ClippedException(String.format("Receiver with id: %d does not exist!", receiverId)));


            if (buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(sender, receiver) ||
                    buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(receiver, sender))
                throw new ClippedException("Users are already buddies!");

            Buddy buddy = buddyRepository.findBySenderAndReceiverAndAcceptedIsNull(sender, receiver)
                    .orElseThrow(() -> new ClippedException("Buddy request does not exist!"));

            buddy.setAccepted(true);
            buddyRepository.save(buddy);
        } catch (ClippedException e) {
            throw new ClippedException(String.format("Accept buddy request from sender with id: %d to receiver with id: %d failed due to %s",
                    buddyRequestDto.getSenderId(), buddyRequestDto.getReceiverId(), e.getMessage()), e);
        }
    }

    public void declineBuddyRequest(BuddyRequestDto buddyRequestDto) throws ClippedException {
        try {
            long senderId = buddyRequestDto.getSenderId();
            long receiverId = buddyRequestDto.getReceiverId();

            User sender = userService.findById(senderId)
                    .orElseThrow(() -> new ClippedException(String.format("Sender with id: %d does not exist!", senderId)));
            User receiver = userService.findById(receiverId)
                    .orElseThrow(() -> new ClippedException(String.format("Receiver with id: %d does not exist!", receiverId)));

            if (buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(sender, receiver) ||
                    buddyRepository.existsBySenderAndReceiverAndAcceptedIsTrue(receiver, sender))
                throw new ClippedException("Users are already buddies!");

            Buddy buddy = buddyRepository.findBySenderAndReceiverAndAcceptedIsNull(sender, receiver)
                    .orElseThrow(() -> new ClippedException("Buddy request does not exist!"));

            buddy.setAccepted(false);
            buddyRepository.save(buddy);
        } catch (ClippedException e) {
            throw new ClippedException(String.format("Decline buddy request from sender with id: %d to receiver with id: %d failed due to %s",
                    buddyRequestDto.getSenderId(), buddyRequestDto.getReceiverId(), e.getMessage()), e);
        }
    }

    public void removeBuddy(BuddyRequestDto buddyRequestDto) throws ClippedException {
        try {
            long senderId = buddyRequestDto.getSenderId();
            long receiverId = buddyRequestDto.getReceiverId();

            User sender = userService.findById(senderId)
                    .orElseThrow(() -> new ClippedException(String.format("Sender with id: %d does not exist!", senderId)));
            User receiver = userService.findById(receiverId)
                    .orElseThrow(() -> new ClippedException(String.format("Receiver with id: %d does not exist!", receiverId)));

            Optional<Buddy> senderToReceiverBuddy = buddyRepository.findBySenderAndReceiverAndAcceptedIsTrue(sender, receiver);
            Optional<Buddy> receiverToSenderBuddy = buddyRepository.findBySenderAndReceiverAndAcceptedIsTrue(receiver, sender);

            if (senderToReceiverBuddy.isPresent()) {
                buddyRepository.delete(senderToReceiverBuddy.get());
            } else if (receiverToSenderBuddy.isPresent()) {
                buddyRepository.delete(receiverToSenderBuddy.get());
            } else {
                throw new ClippedException(String.format("Sender with id: %d and receiver with id: %d are not buddies!", senderId, receiverId));
            }
        } catch (ClippedException e) {
            throw new ClippedException(String.format("Remove buddy from sender with id: %d to receiver with id: %d failed due to %s",
                    buddyRequestDto.getSenderId(), buddyRequestDto.getReceiverId(), e.getMessage()), e);
        }
    }

    public Set<BuddyDto> getPendingBuddyRequests(long userId) throws ClippedException {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new ClippedException(String.format("User with id: %d does not exist!", userId)));

            // TODO: Use a linked hash set maybe or order the buddy requests by created date time descending
            return BuddyMapper.INSTANCE.mapToDtos(buddyRepository.findByReceiverAndAcceptedIsNull(user));
        } catch (ClippedException e) {
            throw new ClippedException(String.format("Get pending buddy requests for user with id: %d failed due to %s",
                    userId, e.getMessage()), e);
        }
    }

    private void saveBuddyRequest(User sender, User receiver) {
        Buddy buddy = new Buddy();

        buddy.setSender(sender);
        buddy.setReceiver(receiver);

        buddyRepository.save(buddy);
    }
}
