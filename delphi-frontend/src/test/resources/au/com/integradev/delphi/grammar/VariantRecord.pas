unit VariantRecord;

interface

type
  SimpleVariantRecord = record
    case Integer of
      0: (Data: array[0..3] of Word);
      1: (High, Low: Longint);
  end;

  VariantRecordWithMethods = record
    function SomeFunction(SomeArgument: SomeType): String;
    procedure SomeProcedure(SomeArgument: SomeType);

    case Integer of
      0: (Data: array[0..3] of Word);
      1: (High, Low: Longint);
  end;

  VariantRecordWithAnonymousEnumTag = record
    case Tag: (vtWord, vtLongint) of
      vtWord: (Data: array[0..3] of Word);
      vtLongint: (High, Low: Longint);
  end;

  VariantRecordWithSubRangeTag = record
    case Tag: 0..1 of
      0: (Data: array[0..3] of Word);
      1: (High, Low: Longint);
  end;


implementation

end.
