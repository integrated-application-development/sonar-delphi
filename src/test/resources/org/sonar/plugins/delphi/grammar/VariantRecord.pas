unit VariantRecord;

interface

type
  MySnazzyRecord = record
    case Integer of
      0: (Data: array[0..3] of Word);
      1: (High, Low: Longint);
  end;

implementation

end.
