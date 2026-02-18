#include "point.h"
#include "stdlib.h"

point_t *point_new(float x, float y) {
    point_t *point = (point_t *) malloc(sizeof(point_t));
    point->x = x;
    point->y = y;
    return point;
}

void point_add_mut(point_t *to, point_t value) {
    to->x += value.x;
    to->y += value.y;
}

point_t point_add(point_t a, point_t b) {
    return (point_t) {.x = a.x + b.x, .y = a.y + b.y};
}

point_array_t *point_array_new(size_t count) {
    point_t *data = (point_t *) calloc(count, sizeof(point_t));

    for (size_t i = 0; i < count; i++) {
        data[i].x = (float)i;
        data[i].y = (float)i;
    }

    point_array_t *array = (point_array_t *) malloc(sizeof(point_array_t));
    array->data = data;
    array->count = count;
    return array;
}
