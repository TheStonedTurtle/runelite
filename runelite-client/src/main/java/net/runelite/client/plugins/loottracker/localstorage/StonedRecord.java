/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.loottracker.localstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;

@Getter
@Deprecated
public class StonedRecord
{
	private final int id;
	private final String name;
	private final int level;
	private final int killCount;
	final Collection<StonedItemEntry> drops;

	public StonedRecord(int id, String name, int level, int kc, Collection<StonedItemEntry> drops)
	{
		this.id = id;
		this.name = name;
		this.level = level;
		this.killCount = kc;
		this.drops = (drops == null ? new ArrayList<>() : drops);
	}

	LTRecord toNewFormat()
	{
		List<LTItemEntry> drops = new ArrayList<>();
		for (StonedItemEntry e : this.getDrops())
		{
			drops.add(new LTItemEntry(e.getName(), e.getId(), e.getQuantity(), e.getPrice()));
		}

		return new LTRecord(this.getId(), this.getName(), this.getLevel(), this.getKillCount(), drops);
	}
}
