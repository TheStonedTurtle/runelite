/*
 * Copyright (c) 2018, TheStonedTurtle <http://github.com/TheStonedTurtle>
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
package net.runelite.http.api.database;

import java.util.ArrayList;

public class LootRecord
{
	private final int npcID;
	private final String npcName;
	private final int killCount;
	private final ArrayList<DropEntry> drops;

	LootRecord(int id, String name, int kc, ArrayList<DropEntry> drops)
	{
		this.npcID = id;
		this.npcName = name;
		this.killCount = kc;
		this.drops = drops;
	}

	LootRecord(int id, String name, ArrayList<DropEntry> drops)
	{
		this.npcID = id;
		this.npcName = name;
		this.killCount = -1;
		this.drops = drops;
	}


	@Override
	public String toString()
	{
		StringBuilder m = new StringBuilder();
		m.append("LootRecord{npcID=").append(npcID).append(", npcName=").append(npcName).append(", killCount=").append(killCount).append(", drops=[");
		if (drops != null)
		{
			boolean first = true;
			for (DropEntry d : drops)
			{
				if (!first)
					m.append(", ");

				m.append(d.toString());
				first = false;
			}
			m.append("]");
		}
		else
		{
			m.append("null]");
		}
		m.append("}");

		return m.toString();
	}
}
