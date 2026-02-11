#ifndef POINT_H
#define POINT_H

typedef struct {
    float x;
    float y;
} point_t;

__declspec(dllexport) point_t *pointNew(float x, float y);


__declspec(dllexport) point_t *pointsNew(int *count);

__declspec(dllexport) void point_add_mut(point_t *to, point_t b);

__declspec(dllexport) point_t point_add(point_t a, point_t b);

#endif POINT_H