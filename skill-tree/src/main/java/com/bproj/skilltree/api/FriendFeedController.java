package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.service.AchievementService;
import com.bproj.skilltree.service.ActivityService;
import com.bproj.skilltree.service.FriendshipService;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.util.AuthUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * End point for retrieving the Friend Actions feed.
 */
@RestController
@RequestMapping("/api/feed/friends")
public class FriendFeedController {
  private static final Logger logger = LoggerFactory.getLogger(FriendFeedController.class);
  private final FriendshipService friendshipService;
  private final ActivityService activityService;
  private final TreeService treeService;
  private final AchievementService achievementService;
  private final AuthUtils authUtils;

  /**
   * Explicit value constructor.
   *
   * @param friendshipService FriendshipService
   * @param activityService ActivityService
   * @param treeService TreeService
   * @param achievementService AchievementService
   * @param authUtils Authentication Utilities
   */
  public FriendFeedController(FriendshipService friendshipService, ActivityService activityService,
      TreeService treeService, AchievementService achievementService, AuthUtils authUtils) {
    this.friendshipService = friendshipService;
    this.activityService = activityService;
    this.treeService = treeService;
    this.achievementService = achievementService;
    this.authUtils = authUtils;
  }

  /**
   * Retrieve the Friend Actions Feed for the end user.
   *
   * @param auth JWT
   * @param days The number of days from the present to search for FeedItems
   * @return The list of FeedItems
   */
  @GetMapping
  public ResponseEntity<List<FeedItem>> getFriendFeed(Authentication auth,
      @RequestParam(defaultValue = "14") int days) {
    logger.info("GET /api/feed/friends - getFriendFeed(days={})", days);
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    List<ObjectId> friendIds = friendshipService.getFriendIds(userId);
    List<FeedItem> friendActions = new ArrayList<>();
    friendActions.addAll(treeService.getTreeFeedItemsByUserIds(friendIds, days));
    friendActions.addAll(achievementService.getAchievementFeedItemsByUserIds(friendIds, days));
    friendActions.addAll(activityService.getActivityFeedItemsByUserIds(friendIds, days));
    friendActions.sort(Comparator.comparing(FeedItem::getPostedAt).reversed());
    return ResponseEntity.ok(friendActions);
  }
}
