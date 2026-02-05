#ifndef POINT_H
#define POINT_H

typedef struct {
    float x;
    float y;
} point_t;

__declspec(dllexport) void point_add(point_t *to, point_t b);

#endif POINT_H