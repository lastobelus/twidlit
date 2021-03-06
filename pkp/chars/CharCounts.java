/**
 * Copyright 2015 Pushkar Piggott
 *(
 * CharCounts.java
 */
package pkp.chars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import pkp.io.Io;
import pkp.io.CrLf;
import pkp.lookup.SharedIndexableInts;
import pkp.util.Pref;
import pkp.util.Log;

////////////////////////////////////////////////////////////////////////////////
class CharCounts implements SharedIndexableInts {
   
   ////////////////////////////////////////////////////////////////////////////
   static int sm_CHARS = 128;
   
   ////////////////////////////////////////////////////////////////////////////
   CharCounts(boolean bigrams) {
      m_MAX_REPEAT = Pref.getInt("#.count.repeats.max", 2);
      m_Counts = new int[bigrams
                         ? (sm_CHARS + 1) * sm_CHARS
                         : sm_CHARS];
      m_CrLf = new CrLf();
      m_Repeat = 0;
      m_Bigrams = bigrams;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   public boolean hasBigramCounts() {
      return m_Bigrams;
   }

   ////////////////////////////////////////////////////////////////////////////
   @Override // SharedIndexableInts
   public int getSize() {
      return sm_CHARS;
   }

   ////////////////////////////////////////////////////////////////////////////
   @Override // SharedIndexableInts
   public int getCount(int i) {
      return m_Counts[i];
   }

   ////////////////////////////////////////////////////////////////////////////
   @Override // SharedIndexableInts
   public String getLabel(int i) {
      return Io.toEscapeCharCommented((char)i);
   }

   ////////////////////////////////////////////////////////////////////////////
   public void nextChar(char c) {
      char prev = m_CrLf.getPrev();
      c = m_CrLf.next(c);
      if (c == '\0') {
         return;
      }
      if (c != prev) {
         m_Repeat = 0;
      } else {
         ++m_Repeat;
         if (m_Repeat >= m_MAX_REPEAT) {
//System.out.printf("Ignoring %dth repeat%n", m_Repeat);               
            return;
         }
      }
      ++m_Counts[c];
      if (m_Bigrams && prev != 0) {
         ++m_Counts[combine(prev, c)];
//System.out.printf("\"%c%c\" combine(prev, c) %5d m_Counts[combine(prev, c)] %3d%n", prev, c, combine(prev, c), m_Counts[combine(prev, c)]);               
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   class BigramCounts implements SharedIndexableInts {
      
      /////////////////////////////////////////////////////////////////////////
      @Override // SharedIndexableInts
      public int getSize() {
         return m_Counts.length - sm_CHARS;
      }

      /////////////////////////////////////////////////////////////////////////
      @Override // SharedIndexableInts
      public int getCount(int i) {
         return m_Counts[i + sm_CHARS];
      }

      /////////////////////////////////////////////////////////////////////////
      @Override // SharedIndexableInts
      public String getLabel(int i) {
         return Io.toEscapeCharCommented((char)(i / sm_CHARS)) + Io.toEscapeCharCommented((char)(i % sm_CHARS));
      }
   }
   
   // Private /////////////////////////////////////////////////////////////////

   ////////////////////////////////////////////////////////////////////////////
   private static int combine(int first, int second) {
      return (first + 1) * sm_CHARS + second;   
   }
   
   // Data ////////////////////////////////////////////////////////////////////
   private static int m_MAX_REPEAT;
   private int[] m_Counts;
   private CrLf m_CrLf;
   private int m_Repeat;
   private boolean m_Bigrams;
}
