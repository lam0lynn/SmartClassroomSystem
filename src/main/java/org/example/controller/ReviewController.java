package org.example.controller;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.pojo.Review;
import org.example.service.ReviewService;
@CrossOrigin
@RestController
@RequestMapping("/reviews")


@Mapper
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 插入新评价
    @PostMapping("/add")
    public void addReview(@RequestBody Review review) {
        reviewService.insert(review);
    }

    // 更新评价信息
    @PutMapping("/{id}")
    public void updateReview(@PathVariable Integer id, @RequestBody Review review) {
        review.setId(id);
        reviewService.update(review);
    }

    // 删除评价
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewService.delete(id);
    }

    // 动态查询评价
    @PostMapping("/search")
    public List<Review> findAllReviews(@RequestBody Review review) {
        return reviewService.findAllReview(review);
    }

    // 根据评分查找教室列表
    @GetMapping("/classrooms/rating/{rating}")
    public List<Integer> findClassroomsByRating(@PathVariable Integer rating) {
        return reviewService.findClassroomIdsByRating(rating);
    }

    @GetMapping("")
    public List<Review> findAllReviews() {
        return reviewService.findAllReview(new Review());
    }

    @GetMapping("/classroom/{classroomId}")
    public List<ReviewVO> findReviewsByClassroomId(@PathVariable Integer classroomId) {
        return reviewService.findReviewsByClassroomId(classroomId);
    }

}



