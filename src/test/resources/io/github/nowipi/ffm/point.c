#include "point.h"
#include "stdlib.h"

point_t *pointNew(float x, float y) {
    point_t *point = (point_t *) malloc(sizeof(point_t));
    point->x = 0;
    point->y = 0;
    return point;
}

point_t *pointsNew(int *count) {
    const int point_count = 10;
    point_t *points = (point_t *) calloc(point_count, sizeof(point_t));

    for (int i = 0; i < point_count; i++) {
        point_t point = points[i];
        point.x = i;
        point.y = i;
    }
    return points;
}

void point_add_mut(point_t *to, point_t value) {
    to->x += value.x;
    to->y += value.y;
}

point_t point_add(point_t a, point_t b) {
    return (point_t) {.x = a.x + b.x, .y = a.y + b.y};
}
