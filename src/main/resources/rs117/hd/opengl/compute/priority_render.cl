/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Calculate adjusted priority for a face with a given priority, distance, and
// model global min10 and face distance averages. This allows positioning faces
// with priorities 10/11 into the correct 'slots' resulting in 18 possible
// adjusted priorities
int priority_map(int p, int distance, int _min10, int avg1, int avg2, int avg3) {
  // (10, 11)  0  1  2  (10, 11)  3  4  (10, 11)  5  6  7  8  9  (10, 11)
  //   0   1   2  3  4    5   6   7  8    9  10  11 12 13 14 15   16  17
  switch (p) {
    case 0: return 2;
    case 1: return 3;
    case 2: return 4;
    case 3: return 7;
    case 4: return 8;
    case 5: return 11;
    case 6: return 12;
    case 7: return 13;
    case 8: return 14;
    case 9: return 15;
    case 10:
      if (distance > avg1) {
        return 0;
      } else if (distance > avg2) {
        return 5;
      } else if (distance > avg3) {
        return 9;
      } else {
        return 16;
      }
    case 11:
      if (distance > avg1 && _min10 > avg1) {
        return 1;
      } else if (distance > avg2 && (_min10 > avg1 || _min10 > avg2)) {
        return 6;
      } else if (distance > avg3 && (_min10 > avg1 || _min10 > avg2 || _min10 > avg3)) {
        return 10;
      } else {
        return 17;
      }
    default:
      // this can't happen unless an invalid priority is sent. just assume 0.
      return 0;
  }
}

// calculate the number of faces with a lower adjusted priority than
// the given adjusted priority
int count_prio_offset(__local struct shared_data *shared, int priority) {
  // this shouldn't ever be outside of (0, 17) because it is the return value from priority_map
  priority = clamp(priority, 0, 17);
  int total = 0;
  for (int i = 0; i < priority; i++) {
    total += shared->totalMappedNum[i];
  }
  return total;
}

void get_face(
  __local struct shared_data *shared,
  __constant struct uniform *uni,
  __global const struct VertexInfo *vb,
  __global const struct VertexInfo *tempvb,
  int localId, struct modelinfo minfo, int cameraYaw, int cameraPitch,
  /* out */ int *prio, int *dis,
  /* out */ struct VertexInfo *oA, struct VertexInfo *oB, struct VertexInfo *oC) {
  int size = minfo.size;
  int offset = minfo.offset;
  int flags = minfo.flags;
  int ssboOffset;

  if (localId < size) {
    ssboOffset = localId;
  } else {
    ssboOffset = 0;
  }

  // Grab triangle vertices from the correct buffer
  if (flags < 0) {
    *oA = vb[offset + ssboOffset * 3];
    *oB = vb[offset + ssboOffset * 3 + 1];
    *oC = vb[offset + ssboOffset * 3 + 2];
  } else {
    *oA = tempvb[offset + ssboOffset * 3];
    *oB = tempvb[offset + ssboOffset * 3 + 1];
    *oC = tempvb[offset + ssboOffset * 3 + 2];
  }

  if (localId < size) {
    int radius = (flags & 0x7fffffff) >> 12;
    int orientation = flags & 0x7ff;
    *prio = (oA->position.w >> 16) & 0xff;// all vertices on the face have the same priority

    // rotate for model orientation
    oA->position = rotate_vertex(uni, oA->position, orientation);
    oB->position = rotate_vertex(uni, oB->position, orientation);
    oC->position = rotate_vertex(uni, oC->position, orientation);

    // calculate distance to face
    if (radius == 0) {
      *dis = 0;
    } else {
      *dis = face_distance(oA->position, oB->position, oC->position, cameraYaw, cameraPitch) + radius;
    }
  } else {
    *oA = (struct VertexInfo) { 0, 0 };
    *oB = (struct VertexInfo) { 0, 0 };
    *oC = (struct VertexInfo) { 0, 0 };
    *prio = 0;
    *dis = 0;
  }
}

void add_face_prio_distance(
  __local struct shared_data *shared,
  __constant struct uniform *uni,
  uint localId, struct modelinfo minfo,
  struct VertexInfo *thisA, struct VertexInfo *thisB, struct VertexInfo *thisC,
  int thisPriority, int thisDistance, int4 pos) {
  if (localId < minfo.size) {
    // if the face is not culled, it is calculated into priority distance averages
    if (face_visible(uni, thisA->position, thisB->position, thisC->position, pos)) {
      atomic_add(&shared->totalNum[thisPriority], 1);
      atomic_add(&shared->totalDistance[thisPriority], thisDistance);

      // calculate minimum distance to any face of priority 10 for positioning the 11 faces later
      if (thisPriority == 10) {
        atomic_min(&shared->min10, thisDistance);
      }
    }
  }
}

int map_face_priority(__local struct shared_data *shared, int localId, struct modelinfo minfo, int thisPriority, int thisDistance, int *prio) {
  int size = minfo.size;

  // Compute average distances for 0/2, 3/4, and 6/8

  if (localId < size) {
    int avg1 = 0;
    int avg2 = 0;
    int avg3 = 0;

    if (shared->totalNum[1] > 0 || shared->totalNum[2] > 0) {
      avg1 = (shared->totalDistance[1] + shared->totalDistance[2]) / (shared->totalNum[1] + shared->totalNum[2]);
    }

    if (shared->totalNum[3] > 0 || shared->totalNum[4] > 0) {
      avg2 = (shared->totalDistance[3] + shared->totalDistance[4]) / (shared->totalNum[3] + shared->totalNum[4]);
    }

    if (shared->totalNum[6] > 0 || shared->totalNum[8] > 0) {
      avg3 = (shared->totalDistance[6] + shared->totalDistance[8]) / (shared->totalNum[6] + shared->totalNum[8]);
    }

    int adjPrio = priority_map(thisPriority, thisDistance, shared->min10, avg1, avg2, avg3);
    int prioIdx = atomic_add(&shared->totalMappedNum[adjPrio], 1);

    *prio = adjPrio;
    return prioIdx;
  }

  *prio = 0;
  return 0;
}

void insert_dfs(__local struct shared_data *shared, int localId, struct modelinfo minfo, int adjPrio, int distance, int prioIdx) {
  int size = minfo.size;

  if (localId < size) {
    // calculate base offset into dfs based on number of faces with a lower priority
    int baseOff = count_prio_offset(shared, adjPrio);
    // store into face array offset array by unique index
    shared->dfs[baseOff + prioIdx] = localId << 16 | distance;
  }
}

void sort_and_insert(
  __local struct shared_data *shared,
  __global const float4 *uv,
  __global const float4 *tempuv,
  __global struct VertexInfo *vout,
  __global float4 *uvout,
  __constant struct uniform *uni,
  int localId, struct modelinfo minfo, int thisPriority, int thisDistance,
  struct VertexInfo *thisA, struct VertexInfo *thisB, struct VertexInfo *thisC) {
  /* compute face distance */
  int size = minfo.size;

  if (localId < size) {
    int outOffset = minfo.idx;
    int uvOffset = minfo.uvOffset;
    int flags = minfo.flags;
    int4 pos = (int4)(minfo.x, minfo.y, minfo.z, 0);
    int orientation = flags & 0x7ff;

    const int priorityOffset = count_prio_offset(shared, thisPriority);
    const int numOfPriority = shared->totalMappedNum[thisPriority];
    int start = priorityOffset; // index of first face with this priority
    int end = priorityOffset + numOfPriority; // index of last face with this priority
    int myOffset = priorityOffset;
    
    // we only have to order faces against others of the same priority
    // calculate position this face will be in
    for (int i = start; i < end; ++i) {
      int d1 = shared->dfs[i];
      int theirId = d1 >> 16;
      int theirDistance = d1 & 0xffff;

      // the closest faces draw last, so have the highest index
      // if two faces have the same distance, the one with the
      // higher id draws last
      if ((theirDistance > thisDistance)
        || (theirDistance == thisDistance && theirId < localId)) {
        ++myOffset;
      }
    }

    thisA->position += pos;
    thisB->position += pos;
    thisC->position += pos;
    thisA->normal.xyz = normalize(thisA->normal.xyz);
    thisB->normal.xyz = normalize(thisB->normal.xyz);
    thisC->normal.xyz = normalize(thisC->normal.xyz);
    thisA->normal = rotate2(uni, thisA->normal, orientation);
    thisB->normal = rotate2(uni, thisB->normal, orientation);
    thisC->normal = rotate2(uni, thisC->normal, orientation);

    // position vertices in scene and write to out buffer
    vout[outOffset + myOffset * 3]     = *thisA;
    vout[outOffset + myOffset * 3 + 1] = *thisB;
    vout[outOffset + myOffset * 3 + 2] = *thisC;

    if (uvOffset < 0) {
      uvout[outOffset + myOffset * 3]     = (float4)(0);
      uvout[outOffset + myOffset * 3 + 1] = (float4)(0);
      uvout[outOffset + myOffset * 3 + 2] = (float4)(0);
    } else if (flags >= 0) {
      uvout[outOffset + myOffset * 3]     = tempuv[uvOffset + localId * 3];
      uvout[outOffset + myOffset * 3 + 1] = tempuv[uvOffset + localId * 3 + 1];
      uvout[outOffset + myOffset * 3 + 2] = tempuv[uvOffset + localId * 3 + 2];
    } else {
      uvout[outOffset + myOffset * 3]     = uv[uvOffset + localId * 3];
      uvout[outOffset + myOffset * 3 + 1] = uv[uvOffset + localId * 3 + 1];
      uvout[outOffset + myOffset * 3 + 2] = uv[uvOffset + localId * 3 + 2];
    }
  }
}
