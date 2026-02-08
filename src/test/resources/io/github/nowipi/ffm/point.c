#include "point.h"

void point_add_mut(point_t *to, point_t value) {
    to->x += value.x;
    to->y += value.y;
}

point_t point_add(point_t a, point_t b) {
    return (point_t) {.x = a.x + b.x, .y = a.y + b.y};
}
