package com.bproj.skilltree.api;

import com.bproj.skilltree.dto.FeedItem;
import com.bproj.skilltree.service.AchievementService;
import com.bproj.skilltree.service.ActivityService;
import com.bproj.skilltree.service.FriendshipService;
import com.bproj.skilltree.service.TreeService;
import com.bproj.skilltree.service.UserService;
import com.bproj.skilltree.util.AuthUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.types.ObjectId;
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
   * @param userService UserService
   */
  public FriendFeedController(FriendshipService friendshipService, ActivityService activityService,
      TreeService treeService, AchievementService achievementService, UserService userService) {
    this.friendshipService = friendshipService;
    this.activityService = activityService;
    this.treeService = treeService;
    this.achievementService = achievementService;
    this.authUtils = new AuthUtils(userService);
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
    ObjectId userId = authUtils.getUserIdByAuth(auth);
    List<ObjectId> friendIds = friendshipService.getFriendIds(userId);
    List<FeedItem> friendActions = new ArrayList<>();
    friendActions.addAll(treeService.getFeedItems(friendIds, days));
    friendActions.addAll(achievementService.getFeedItems(friendIds, days));
    friendActions.addAll(activityService.getFeedItems(friendIds, days));
    friendActions.sort(Comparator.comparing(FeedItem::getPostedAt).reversed());
    return ResponseEntity.ok(friendActions);
  }
}
