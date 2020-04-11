/*
 * Copyright (C) 2020 YouTrack Bot Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * YouTrack is a product released by JetBrains s.r.o.
 * and this project is not affiliated with them.
 */

package pl.fratik.youtrackbot.api.models;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * Zgłoszenie
 */
public interface Issue {
    /**
     * Zwraca załączniki
     * @return załączniki
     */
    List<Attachment> getAttachments();

    /**
     * Zwraca czas stworzenia
     * @returns czas stworzenia
     */
    long getCreated();

    /**
     * Zwraca opis zgłoszenia
     * @return opis zgłoszenia
     */
    String getDescription();

    /**
     * Zwraca projekt
     * @return projekt
     */
    Project getProject();

    /**
     * Zwraca pola z prawej
     * @return pola z prawej
     */
    List<Field> getFields();

    /**
     * Zwraca ID w formacie "(Project.shortName)-(numer)"
     * @return ID w ludzkim formacie
     */
    String getIdReadable();

    /**
     * Zwraca osobę zgłaszającą
     * @return osoba zgłaszająca
     */
    User getReporter();

    /**
     * Zwraca skrót zgłoszenia
     * @return skrót zgłoszenia
     */
    String getSummary();

    /**
     * Zwraca wewnętrzne ID zgłoszenia
     * @return wewnętrzne ID zgłoszenia
     */
    String getId();

    /**
     * Załącznik
     */
    interface Attachment {
        /**
         * Zwraca adres URL załącznika
         * @return adres URL załącznika
         */
        String getUrl();

        /**
         * Zwraca nazwę załącznika
         * @return nazwa załącznika
         */
        String getName();
    }

    /**
     * Pole po prawej
     */
    interface Field {
        /**
         * Zwraca nazwę tego pola
         * @return nazwa tego pola
         */
        String getName();

        /**
         * Zwraca wewnętrzne ID tego pola
         * @return wewnętrzne ID tego pola
         */
        String getId();

        /**
         * Zwraca wartości tego pola
         * @return wartości tego pola
         */
        List<FieldValue> getValue();

        /**
         * Wartość tegoż pola
         */
        interface FieldValue {
            /**
             * Zwraca wartość pola
             * @return wartość pola
             */
            String getName();

            /**
             * Zwraca opis wartości pola
             * @return opis wartości pola
             */
            String getDescription();

            /**
             * Zwraca kolor wartości pola
             * @return kolor wartości pola
             */
            FieldValueColor getColor();

            /**
             * Kolor wartości pola
             */
            interface FieldValueColor {

                /**
                 * Zwraca kolor tła
                 * @return kolor tła
                 */
                default Color getBackground() {
                    return getColor(getBackgroundRaw());
                }

                /**
                 * Zwraca kolor tekstu
                 * @return kolor tekstu
                 */
                default Color getForeground() {
                    return getColor(getForegroundRaw());
                }

                @NotNull
                default Color getColor(final String color) {
                    String nie = color;
                    if (color.length() == 4) {
                        nie = "#";
                        nie += color.charAt(1);
                        nie += color.charAt(1);
                        nie += color.charAt(2);
                        nie += color.charAt(2);
                        nie += color.charAt(3);
                        nie += color.charAt(3);
                    }
                    return Color.decode(nie);
                }

                /**
                 * Zwraca kolor tła
                 * @return kolor tła w formacie #hex
                 */
                String getBackgroundRaw();

                /**
                 * Zwraca kolor tekstu
                 * @return kolor tekstu w formacie #hex
                 */
                String getForegroundRaw();
            }
        }
    }
}
