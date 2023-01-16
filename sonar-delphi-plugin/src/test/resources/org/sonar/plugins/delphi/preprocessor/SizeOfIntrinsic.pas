unit SizeOfIntrinsic;

interface

{$IF SizeOf(Byte) <> 1}
ERROR
{$ENDIF}

{$IF SizeOf(System.Byte) <> 1}
ERROR
{$ENDIF}

{$IF System.SizeOf(Byte) <> 1}
ERROR
{$ENDIF}

{$IF SizeOf(Integer) <> 4}
ERROR
{$ENDIF}

{$IF SizeOf(System.Integer) <> 4}
ERROR
{$ENDIF}

{$IF System.SizeOf(Integer) <> 4}
ERROR
{$ENDIF}

{$IF SizeOf(TObject) <> SizeOf(Pointer)}
ERROR
{$ENDIF}

{$IF SizeOf(System.TObject) <> SizeOf(System.Pointer)}
ERROR
{$ENDIF}

{$IF System.SizeOf(TObject) <> System.SizeOf(Pointer)}
ERROR
{$ENDIF}

implementation

end.
